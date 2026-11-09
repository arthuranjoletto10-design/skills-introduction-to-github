// Conecta o robô ao WhatsApp usando o WhatsApp Web (sem API paga).
// Na primeira vez, aparece um QR Code no terminal: escaneie com o celular
// da clínica em WhatsApp > Aparelhos conectados > Conectar um aparelho.

const config = require('./config');
process.env.TZ = config.fusoHorario;

const qrcode = require('qrcode-terminal');
const { Client, LocalAuth } = require('whatsapp-web.js');
const Storage = require('./storage');
const { Bot } = require('./bot');

const storage = new Storage();
const bot = new Bot({ config, storage });

const client = new Client({
  authStrategy: new LocalAuth({ clientId: 'clinica' }), // mantém o login salvo
  puppeteer: {
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-dev-shm-usage'],
  },
});

const esperar = (ms) => new Promise((r) => setTimeout(r, ms));

// Mensagens que o próprio robô enviou (para não confundir com o atendente)
const enviadasPeloBot = new Set();

async function enviar(chatId, texto) {
  try {
    const chat = await client.getChatById(chatId);
    await chat.sendStateTyping(); // mostra "digitando..." para parecer natural
    await esperar(Math.min(800 + texto.length * 8, 3000));
    const msg = await client.sendMessage(chatId, texto);
    if (msg?.id?._serialized) enviadasPeloBot.add(msg.id._serialized);
  } catch (e) {
    console.error(`Falha ao enviar para ${chatId}:`, e.message);
  }
}

async function avisarAdmin(texto) {
  if (!config.numeroAdmin || !texto) return;
  await enviar(`${config.numeroAdmin}@c.us`, texto);
}

client.on('qr', (qr) => {
  console.log('\nEscaneie o QR Code abaixo com o WhatsApp da clínica:');
  console.log('(WhatsApp > Aparelhos conectados > Conectar um aparelho)\n');
  qrcode.generate(qr, { small: true });
});

client.on('authenticated', () => console.log('✅ Autenticado.'));
client.on('auth_failure', (m) => console.error('❌ Falha na autenticação:', m));
client.on('disconnected', (motivo) => {
  console.warn('⚠️ Desconectado:', motivo, '- tentando reconectar em 10s...');
  setTimeout(() => client.initialize().catch(console.error), 10_000);
});

client.on('ready', () => {
  console.log(`🤖 Robô da ${config.clinica.nome} está online!`);
});

// Mensagens recebidas dos pacientes
client.on('message', async (msg) => {
  try {
    // Ignora grupos, status, canais e mensagens de sistema
    if (msg.from === 'status@broadcast' || msg.isStatus) return;
    if (msg.from.endsWith('@g.us') || msg.from.endsWith('@newsletter') || msg.from.endsWith('@broadcast')) return;
    if (msg.fromMe) return;

    let texto = msg.body || '';
    if (!texto && msg.hasMedia) texto = '[mídia]';
    if (!texto) return;

    let nome = '';
    try {
      const contato = await msg.getContact();
      nome = contato.pushname || contato.name || '';
    } catch {
      /* sem nome, tudo bem */
    }

    // Áudio, foto etc.: o robô só entende texto
    if (msg.hasMedia && !msg.body) {
      const s = bot.sessao(msg.from);
      if (s.etapa === 'humano') return;
      await enviar(
        msg.from,
        'Recebi seu arquivo! 📎 Mas eu sou um assistente virtual e só entendo mensagens de texto.\n' +
          'Digite *menu* para ver as opções ou *7* para falar com um atendente.',
      );
      return;
    }

    const { respostas, avisoAdmin } = bot.processar(msg.from, texto, nome);
    for (const r of respostas) await enviar(msg.from, r);
    if (avisoAdmin) await avisarAdmin(avisoAdmin);
  } catch (e) {
    console.error('Erro ao processar mensagem:', e);
  }
});

// Mensagens enviadas pelo celular da clínica (atendente humano)
client.on('message_create', async (msg) => {
  if (!msg.fromMe) return;
  if (enviadasPeloBot.has(msg.id._serialized)) {
    enviadasPeloBot.delete(msg.id._serialized);
    return;
  }
  const chatId = msg.to;
  if (!chatId || chatId.endsWith('@g.us')) return;
  const texto = (msg.body || '').trim().toLowerCase();

  if (texto === '#bot') {
    // Atendente devolve a conversa para o robô
    bot.retomarBot(chatId);
    try {
      await msg.delete(true); // apaga o comando para o paciente não ver
    } catch {
      /* ignora */
    }
    console.log(`🤖 Robô reativado para ${chatId}`);
  } else if (texto === '#pausa') {
    bot.pausarBot(chatId);
    try {
      await msg.delete(true);
    } catch {
      /* ignora */
    }
  } else {
    // O atendente respondeu manualmente: o robô para de responder nesse chat.
    // Espera um pouco para não confundir com mensagens do próprio robô ainda em envio.
    await esperar(1500);
    if (enviadasPeloBot.has(msg.id._serialized)) return;
    bot.pausarBot(chatId);
  }
});

// Lembretes da véspera: verifica a cada 10 minutos
setInterval(async () => {
  if (!client.info) return; // ainda não conectado
  for (const l of bot.lembretesPendentes()) {
    await enviar(l.chatId, l.texto);
    storage.marcarLembrete(l.id);
    await esperar(5000); // intervalo entre envios para evitar bloqueio
  }
}, 10 * 60 * 1000);

console.log('Iniciando o WhatsApp... (pode levar alguns segundos)');
client.initialize().catch((e) => {
  console.error('Erro ao iniciar:', e);
  process.exit(1);
});
