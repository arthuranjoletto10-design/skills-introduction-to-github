// =============================================================
//  CONFIGURAÇÕES DA CLÍNICA — edite este arquivo à vontade
// =============================================================

module.exports = {
  clinica: {
    nome: 'Clínica Odontológica Sorriso',
    endereco: 'Rua Exemplo, 123 - Centro - Sua Cidade/UF',
    mapa: 'https://maps.google.com/?q=Rua+Exemplo+123',
    telefone: '(00) 0000-0000',
    instagram: '@clinicasorriso',
  },

  // Número do WhatsApp que recebe avisos de novos agendamentos,
  // cancelamentos e pedidos de atendimento humano.
  // Formato: código do país + DDD + número, sem espaços ou símbolos.
  // Deixe '' para desativar os avisos.
  numeroAdmin: '',

  // Horário de funcionamento. 0 = domingo, 1 = segunda ... 6 = sábado.
  // Dia sem entrada = fechado.
  funcionamento: {
    1: { inicio: '08:00', fim: '18:00' },
    2: { inicio: '08:00', fim: '18:00' },
    3: { inicio: '08:00', fim: '18:00' },
    4: { inicio: '08:00', fim: '18:00' },
    5: { inicio: '08:00', fim: '18:00' },
    6: { inicio: '08:00', fim: '12:00' },
  },

  // Intervalo de almoço (nenhuma consulta é marcada nesse período)
  almoco: { inicio: '12:00', fim: '13:30' },

  // Duração de cada horário da agenda, em minutos
  duracaoConsultaMin: 30,

  // Quantos dias à frente o paciente pode escolher
  diasParaAgendar: 7,

  // Antecedência mínima (em horas) para marcar um horário
  antecedenciaMinimaHoras: 2,

  // Datas em que a clínica não abre (feriados, recesso). Formato AAAA-MM-DD
  feriados: ['2026-12-25', '2027-01-01'],

  // Serviços oferecidos
  servicos: [
    { nome: 'Avaliação / Consulta inicial', preco: 'Gratuita' },
    { nome: 'Limpeza (profilaxia)', preco: 'a partir de R$ 150' },
    { nome: 'Restauração', preco: 'a partir de R$ 180' },
    { nome: 'Clareamento dental', preco: 'a partir de R$ 600' },
    { nome: 'Tratamento de canal', preco: 'sob avaliação' },
    { nome: 'Extração', preco: 'a partir de R$ 200' },
    { nome: 'Ortodontia (aparelho)', preco: 'sob avaliação' },
    { nome: 'Implante dentário', preco: 'sob avaliação' },
  ],

  convenios: ['Amil Dental', 'Bradesco Dental', 'OdontoPrev', 'SulAmérica Odonto', 'Uniodonto'],

  formasPagamento: 'Pix, dinheiro, cartão de débito e crédito (até 10x).',

  // Texto enviado em caso de urgência / dor
  urgencia:
    'Sentimos muito que você esteja com dor! 😟\n' +
    'Durante o horário de funcionamento, encaixamos urgências no mesmo dia.\n' +
    'Fora do horário, ligue para o plantão: (00) 90000-0000.',

  // Depois de quantas horas um atendimento humano "esquecido" volta para o robô
  horasAtendimentoHumano: 12,

  // Depois de quantos minutos sem resposta a conversa volta ao início
  minutosExpirarConversa: 30,

  // Enviar lembrete automático na véspera da consulta?
  lembreteVespera: true,
  // A partir de que horas o lembrete da véspera é enviado
  horaLembrete: '18:00',

  // Fuso horário da clínica
  fusoHorario: 'America/Sao_Paulo',
};
