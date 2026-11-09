// Cálculo de dias e horários disponíveis.
// Todas as datas são tratadas no fuso horário da clínica.

const DIAS_SEMANA = ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

function paraMin(hhmm) {
  const [h, m] = hhmm.split(':').map(Number);
  return h * 60 + m;
}

function paraHHMM(min) {
  return `${String(Math.floor(min / 60)).padStart(2, '0')}:${String(min % 60).padStart(2, '0')}`;
}

// Retorna { data: 'AAAA-MM-DD', minutos: minutos desde 00:00, diaSemana } no fuso da clínica
function agoraNoFuso(fuso, agora = new Date()) {
  const partes = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', {
      timeZone: fuso,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hourCycle: 'h23',
    })
      .formatToParts(agora)
      .map((p) => [p.type, p.value]),
  );
  const data = `${partes.year}-${partes.month}-${partes.day}`;
  return {
    data,
    minutos: Number(partes.hour) * 60 + Number(partes.minute),
    diaSemana: diaDaSemana(data),
  };
}

function diaDaSemana(data) {
  const [a, m, d] = data.split('-').map(Number);
  return new Date(Date.UTC(a, m - 1, d)).getUTCDay();
}

function somarDias(data, n) {
  const [a, m, d] = data.split('-').map(Number);
  const dt = new Date(Date.UTC(a, m - 1, d + n));
  return dt.toISOString().slice(0, 10);
}

function formatarData(data) {
  const [a, m, d] = data.split('-');
  return `${DIAS_SEMANA[diaDaSemana(data)]}, ${d}/${m}/${a}`;
}

function formatarDataCurta(data) {
  const [, m, d] = data.split('-');
  return `${DIAS_SEMANA[diaDaSemana(data)].slice(0, 3)} ${d}/${m}`;
}

// Lista todos os horários de um dia (sem checar ocupação)
function horariosDoDia(config, data) {
  const exp = config.funcionamento[diaDaSemana(data)];
  if (!exp || config.feriados.includes(data)) return [];
  const ini = paraMin(exp.inicio);
  const fim = paraMin(exp.fim);
  const dur = config.duracaoConsultaMin;
  const almIni = config.almoco ? paraMin(config.almoco.inicio) : null;
  const almFim = config.almoco ? paraMin(config.almoco.fim) : null;
  const lista = [];
  for (let t = ini; t + dur <= fim; t += dur) {
    if (almIni !== null && t < almFim && t + dur > almIni) continue;
    lista.push(paraHHMM(t));
  }
  return lista;
}

function horariosLivres(config, storage, data, agora = new Date()) {
  const ref = agoraNoFuso(config.fusoHorario, agora);
  const limite = ref.minutos + config.antecedenciaMinimaHoras * 60;
  return horariosDoDia(config, data).filter((h) => {
    if (storage.horarioOcupado(data, h)) return false;
    if (data === ref.data && paraMin(h) < limite) return false;
    if (data < ref.data) return false;
    return true;
  });
}

// Próximos dias que ainda têm algum horário livre
function diasDisponiveis(config, storage, agora = new Date()) {
  const ref = agoraNoFuso(config.fusoHorario, agora);
  const dias = [];
  for (let i = 0; i < config.diasParaAgendar + 14 && dias.length < config.diasParaAgendar; i++) {
    const data = somarDias(ref.data, i);
    if (horariosLivres(config, storage, data, agora).length > 0) dias.push(data);
  }
  return dias;
}

module.exports = {
  DIAS_SEMANA,
  paraMin,
  agoraNoFuso,
  diaDaSemana,
  somarDias,
  formatarData,
  formatarDataCurta,
  horariosDoDia,
  horariosLivres,
  diasDisponiveis,
};
