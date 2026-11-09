const test = require('node:test');
const assert = require('node:assert');
const config = require('../src/config');
const Storage = require('../src/storage');
const { Bot } = require('../src/bot');
const agenda = require('../src/agenda');

// Segunda-feira, 09/11/2026, 09:00 em São Paulo (12:00 UTC)
const AGORA = new Date('2026-11-09T12:00:00Z');

function novoBot(agora = AGORA) {
  const storage = new Storage(null); // memória
  const relogio = { t: agora };
  const bot = new Bot({ config, storage, agora: () => relogio.t });
  return { bot, storage, relogio };
}

const CHAT = '5511999999999@c.us';
const fala = (bot, txt) => bot.processar(CHAT, txt, 'Maria Silva');

test('primeira mensagem mostra saudação e menu', () => {
  const { bot } = novoBot();
  const r = fala(bot, 'oi');
  assert.match(r.respostas[0], /Bom dia, Maria/);
  assert.match(r.respostas[0], /Agendar consulta/);
});

test('horários respeitam almoço, antecedência e sábado', () => {
  const { storage } = novoBot();
  const seg = agenda.horariosLivres(config, storage, '2026-11-09', AGORA);
  assert.equal(seg[0], '11:00'); // 09:00 + 2h de antecedência
  assert.ok(!seg.includes('12:00') && !seg.includes('13:00'));
  assert.ok(seg.includes('13:30'));
  assert.equal(seg.at(-1), '17:30');
  const sab = agenda.horariosDoDia(config, '2026-11-14');
  assert.equal(sab.at(-1), '11:30');
  assert.deepEqual(agenda.horariosDoDia(config, '2026-11-15'), []); // domingo
});

test('fluxo completo de agendamento e cancelamento', () => {
  const { bot, storage } = novoBot();
  fala(bot, 'oi');
  assert.match(fala(bot, '1').respostas[0], /Qual serviço/);
  assert.match(fala(bot, '2').respostas[0], /Limpeza/);
  const dias = fala(bot, '2').respostas[0]; // terça 10/11
  assert.match(dias, /Terça, 10\/11\/2026/);
  const nome = fala(bot, '14:00').respostas[0];
  assert.match(nome, /14:00/);
  assert.match(fala(bot, '1').respostas[0], /Paciente: Maria Silva/);
  const conf = fala(bot, 'sim');
  assert.match(conf.respostas[0], /Agendamento confirmado/);
  assert.match(conf.avisoAdmin, /Novo agendamento/);
  assert.equal(storage.dados.agendamentos.length, 1);
  assert.equal(storage.dados.agendamentos[0].hora, '14:00');
  assert.ok(storage.horarioOcupado('2026-11-10', '14:00'));

  const lista = fala(bot, '2').respostas[0];
  assert.match(lista, /Ter 10\/11 às 14:00/);
  fala(bot, '1');
  const canc = fala(bot, '1');
  assert.match(canc.respostas[0], /cancelado/);
  assert.equal(storage.dados.agendamentos[0].status, 'cancelado');
});

test('nome sem letras é recusado', () => {
  const { bot } = novoBot();
  ['1', '1', '2', '1'].forEach((m) => fala(bot, m));
  assert.match(fala(bot, '14:00').respostas[0], /nome completo/);
  assert.match(fala(bot, 'Jo').respostas[0], /nome completo/);
  assert.match(fala(bot, 'João Souza').respostas[0], /Confira/);
});

test('horário ocupado não aparece para outro paciente', () => {
  const { bot, storage } = novoBot();
  storage.criarAgendamento({ chatId: 'x', nome: 'X', servico: 'Y', data: '2026-11-10', hora: '08:00' });
  fala(bot, '1');
  fala(bot, '1');
  fala(bot, '2'); // terça
  const r = fala(bot, '08:00');
  assert.match(r.respostas[0], /escolha um número/);
});

test('atalho por palavra-chave e atendimento humano', () => {
  const { bot, relogio } = novoBot();
  const r = fala(bot, 'quero falar com um atendente');
  assert.match(r.respostas[0], /atendentes/);
  assert.ok(r.avisoAdmin);
  assert.deepEqual(fala(bot, 'olá?').respostas, []); // robô em silêncio
  relogio.t = new Date(AGORA.getTime() + 13 * 3600e3);
  assert.ok(fala(bot, 'oi').respostas.length > 0); // volta depois do prazo
});

test('urgência avisa a clínica', () => {
  const { bot } = novoBot();
  const r = fala(bot, 'estou com muita dor de dente');
  assert.match(r.respostas[0], /plantão/);
  assert.match(r.avisoAdmin, /URGÊNCIA/);
});

test('lembrete da véspera', () => {
  const { bot, storage, relogio } = novoBot();
  storage.criarAgendamento({ chatId: CHAT, nome: 'Maria Silva', servico: 'Limpeza', data: '2026-11-10', hora: '09:00' });
  assert.equal(bot.lembretesPendentes().length, 0); // ainda 09:00
  relogio.t = new Date('2026-11-09T21:30:00Z'); // 18:30 em SP
  const l = bot.lembretesPendentes();
  assert.equal(l.length, 1);
  assert.match(l[0].texto, /amanhã/);
  storage.marcarLembrete(l[0].id);
  assert.equal(bot.lembretesPendentes().length, 0);
});
