// Lógica da conversa. Não depende do WhatsApp: recebe um texto e devolve
// as respostas. Assim dá para testar no terminal (npm run simular).

const agenda = require('./agenda');

const ETAPA = {
  INICIO: 'inicio',
  MENU: 'menu',
  SERVICO: 'agendar_servico',
  DIA: 'agendar_dia',
  HORA: 'agendar_hora',
  NOME: 'agendar_nome',
  CONFIRMAR: 'agendar_confirmar',
  CANCELAR_ESCOLHER: 'cancelar_escolher',
  CANCELAR_CONFIRMAR: 'cancelar_confirmar',
  HUMANO: 'humano',
};

function normalizar(txt) {
  return (txt || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^\w\s:]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

const SIM = /^(1|s|sim|ss|isso|confirmo|confirmar|pode|ok|certo|claro|yes)\b/;
const NAO = /^(2|n|nao|no|negativo|cancela)\b/;

class Bot {
  constructor({ config, storage, agora = () => new Date() }) {
    this.config = config;
    this.storage = storage;
    this.agora = agora;
    this.sessoes = new Map();
  }

  sessao(chatId) {
    let s = this.sessoes.get(chatId);
    if (!s) {
      s = { etapa: ETAPA.INICIO, dados: {}, ultimaInteracao: 0, humanoAte: 0 };
      this.sessoes.set(chatId, s);
    }
    return s;
  }

  // Chamado quando a clínica digita #bot na conversa (pelo celular)
  retomarBot(chatId) {
    const s = this.sessao(chatId);
    s.etapa = ETAPA.INICIO;
    s.dados = {};
    s.humanoAte = 0;
  }

  // Chamado quando um atendente responde manualmente: o robô fica quieto
  pausarBot(chatId) {
    const s = this.sessao(chatId);
    s.etapa = ETAPA.HUMANO;
    s.humanoAte = this.agora().getTime() + this.config.horasAtendimentoHumano * 3600e3;
  }

  /**
   * Processa uma mensagem do paciente.
   * @returns {{ respostas: string[], avisoAdmin?: string }}
   */
  processar(chatId, texto, nomeContato = '') {
    const s = this.sessao(chatId);
    const agora = this.agora().getTime();
    const t = normalizar(texto);
    const out = { respostas: [] };

    // Atendimento humano em andamento: robô fica em silêncio
    if (s.etapa === ETAPA.HUMANO) {
      if (agora < s.humanoAte && t !== 'menu') return out;
      s.etapa = ETAPA.INICIO;
    }

    // Conversa parada há muito tempo volta ao começo
    if (
      s.etapa !== ETAPA.INICIO &&
      agora - s.ultimaInteracao > this.config.minutosExpirarConversa * 60e3
    ) {
      s.etapa = ETAPA.INICIO;
      s.dados = {};
    }
    s.ultimaInteracao = agora;
    if (nomeContato) s.nomeContato = nomeContato;

    // Comandos que funcionam em qualquer etapa
    if (['menu', '0', 'voltar', 'inicio', 'comecar'].includes(t)) {
      s.etapa = ETAPA.MENU;
      s.dados = {};
      out.respostas.push(this.textoMenu());
      return out;
    }
    if (['sair', 'encerrar', 'tchau', 'obrigado', 'obrigada', 'valeu'].includes(t)) {
      s.etapa = ETAPA.INICIO;
      s.dados = {};
      out.respostas.push(
        `Nós que agradecemos! 😊 Quando precisar, é só mandar uma mensagem.\n*${this.config.clinica.nome}*`,
      );
      return out;
    }

    switch (s.etapa) {
      case ETAPA.INICIO:
        return this.etapaInicio(s, t, out, chatId);
      case ETAPA.MENU:
        return this.etapaMenu(s, t, out, chatId);
      case ETAPA.SERVICO:
        return this.etapaServico(s, t, out);
      case ETAPA.DIA:
        return this.etapaDia(s, t, out);
      case ETAPA.HORA:
        return this.etapaHora(s, t, out);
      case ETAPA.NOME:
        return this.etapaNome(s, texto, out);
      case ETAPA.CONFIRMAR:
        return this.etapaConfirmar(s, t, out, chatId);
      case ETAPA.CANCELAR_ESCOLHER:
        return this.etapaCancelarEscolher(s, t, out);
      case ETAPA.CANCELAR_CONFIRMAR:
        return this.etapaCancelarConfirmar(s, t, out, chatId);
      default:
        s.etapa = ETAPA.MENU;
        out.respostas.push(this.textoMenu());
        return out;
    }
  }

  // ------------------------------------------------------------------ textos

  saudacao() {
    const { minutos } = agenda.agoraNoFuso(this.config.fusoHorario, this.agora());
    if (minutos < 12 * 60) return 'Bom dia';
    if (minutos < 18 * 60) return 'Boa tarde';
    return 'Boa noite';
  }

  textoMenu() {
    return [
      'Como posso te ajudar? Digite o *número* da opção:',
      '',
      '1️⃣ Agendar consulta',
      '2️⃣ Meus agendamentos / cancelar',
      '3️⃣ Serviços e valores',
      '4️⃣ Endereço e horário de funcionamento',
      '5️⃣ Convênios e formas de pagamento',
      '6️⃣ Estou com dor / urgência',
      '7️⃣ Falar com um atendente',
      '',
      '_A qualquer momento, digite *menu* para voltar aqui._',
    ].join('\n');
  }

  textoServicos() {
    const linhas = this.config.servicos.map((sv) => `• ${sv.nome} — ${sv.preco}`);
    return `🦷 *Nossos serviços*\n\n${linhas.join('\n')}\n\n_Valores podem variar conforme avaliação._`;
  }

  textoEndereco() {
    const { clinica, funcionamento, almoco } = this.config;
    const dias = [1, 2, 3, 4, 5, 6, 0].map((d) => {
      const f = funcionamento[d];
      return `• ${agenda.DIAS_SEMANA[d]}: ${f ? `${f.inicio} às ${f.fim}` : 'fechado'}`;
    });
    return [
      `📍 *${clinica.nome}*`,
      clinica.endereco,
      clinica.mapa,
      '',
      '🕒 *Horário de funcionamento*',
      ...dias,
      almoco ? `_(Pausa para almoço: ${almoco.inicio} às ${almoco.fim})_` : null,
      '',
      `📞 ${clinica.telefone}${clinica.instagram ? `  |  📷 ${clinica.instagram}` : ''}`,
    ]
      .filter((l) => l !== null)
      .join('\n');
  }

  textoConvenios() {
    return [
      '💳 *Convênios aceitos*',
      ...this.config.convenios.map((c) => `• ${c}`),
      '',
      `*Formas de pagamento:* ${this.config.formasPagamento}`,
    ].join('\n');
  }

  rodape() {
    return '\nDigite *menu* para ver as opções novamente.';
  }

  // ------------------------------------------------------------------ etapas

  etapaInicio(s, t, out, chatId) {
    // Atalhos por palavra-chave mesmo sem passar pelo menu
    const atalho = /^[1-7]$/.test(t) ? t : this.detectarIntencao(t);
    if (atalho) return this.executarOpcao(atalho, s, out, chatId);

    const nome = s.nomeContato ? `, ${s.nomeContato.split(' ')[0]}` : '';
    out.respostas.push(
      `${this.saudacao()}${nome}! 👋 Seja bem-vindo(a) à *${this.config.clinica.nome}*. ` +
        'Sou o assistente virtual e estou aqui para te ajudar.\n\n' +
        this.textoMenu(),
    );
    s.etapa = ETAPA.MENU;
    return out;
  }

  detectarIntencao(t) {
    if (/\b(agendar|agenda|marcar|consulta|horario disponivel)\b/.test(t)) return '1';
    if (/\b(desmarcar|cancelar|remarcar|meus agendamentos)\b/.test(t)) return '2';
    if (/\b(preco|precos|valor|valores|quanto custa|servicos?)\b/.test(t)) return '3';
    if (/\b(endereco|onde fica|localizacao|horario de funcionamento|abre|fecha)\b/.test(t)) return '4';
    if (/\b(convenio|convenios|plano|pagamento|pix|cartao|parcela)\b/.test(t)) return '5';
    if (/\b(dor|doendo|urgencia|urgente|emergencia|sangr\w*|inchad\w*|quebrou)\b/.test(t)) return '6';
    if (/\b(atendente|humano|pessoa|falar com alguem|recepcao)\b/.test(t)) return '7';
    return null;
  }

  etapaMenu(s, t, out, chatId) {
    const opcao = /^[1-7]$/.test(t) ? t : this.detectarIntencao(t);
    if (!opcao) {
      out.respostas.push('Desculpe, não entendi. 😅\n\n' + this.textoMenu());
      return out;
    }
    return this.executarOpcao(opcao, s, out, chatId);
  }

  executarOpcao(opcao, s, out, chatId) {
    s.dados = {};
    switch (opcao) {
      case '1': {
        s.etapa = ETAPA.SERVICO;
        const lista = this.config.servicos.map((sv, i) => `*${i + 1}* - ${sv.nome}`);
        out.respostas.push(`Ótimo! Vamos agendar. 📅\nQual serviço você deseja?\n\n${lista.join('\n')}`);
        return out;
      }
      case '2':
        return this.listarAgendamentos(s, out, chatId);
      case '3':
        s.etapa = ETAPA.MENU;
        out.respostas.push(this.textoServicos() + '\n\nDigite *1* para agendar uma avaliação.' + this.rodape());
        return out;
      case '4':
        s.etapa = ETAPA.MENU;
        out.respostas.push(this.textoEndereco() + '\n' + this.rodape());
        return out;
      case '5':
        s.etapa = ETAPA.MENU;
        out.respostas.push(this.textoConvenios() + '\n' + this.rodape());
        return out;
      case '6':
        s.etapa = ETAPA.HUMANO;
        s.humanoAte = this.agora().getTime() + this.config.horasAtendimentoHumano * 3600e3;
        out.respostas.push(
          this.config.urgencia + '\n\nJá avisei nossa equipe e alguém vai falar com você em instantes.',
        );
        out.avisoAdmin = `🚨 *URGÊNCIA* — ${s.nomeContato || 'Paciente'} (${numeroDe(chatId)}) relatou dor/urgência.`;
        return out;
      case '7':
        s.etapa = ETAPA.HUMANO;
        s.humanoAte = this.agora().getTime() + this.config.horasAtendimentoHumano * 3600e3;
        out.respostas.push(
          'Certo! 🙋 Um de nossos atendentes vai continuar a conversa com você em breve.\n' +
            'Por favor, aguarde. Se quiser voltar ao assistente virtual, digite *menu*.',
        );
        out.avisoAdmin = `🙋 ${s.nomeContato || 'Paciente'} (${numeroDe(chatId)}) pediu para falar com um atendente.`;
        return out;
      default:
        s.etapa = ETAPA.MENU;
        out.respostas.push(this.textoMenu());
        return out;
    }
  }

  etapaServico(s, t, out) {
    const n = Number(t);
    const servicos = this.config.servicos;
    let servico = Number.isInteger(n) && n >= 1 && n <= servicos.length ? servicos[n - 1] : null;
    if (!servico && t.length >= 4) {
      servico = servicos.find((sv) => normalizar(sv.nome).includes(t));
    }
    if (!servico) {
      out.respostas.push(`Por favor, digite um número de *1* a *${servicos.length}*.`);
      return out;
    }
    s.dados.servico = servico.nome;
    return this.mostrarDias(s, out);
  }

  mostrarDias(s, out) {
    const dias = agenda.diasDisponiveis(this.config, this.storage, this.agora());
    if (dias.length === 0) {
      s.etapa = ETAPA.MENU;
      out.respostas.push(
        'Poxa, não temos horários livres nos próximos dias. 😕\nDigite *7* para falar com um atendente.',
      );
      return out;
    }
    s.dados.dias = dias;
    s.etapa = ETAPA.DIA;
    const lista = dias.map((d, i) => `*${i + 1}* - ${agenda.formatarData(d)}`);
    out.respostas.push(`*${s.dados.servico}* ✅\n\nEscolha o dia:\n\n${lista.join('\n')}`);
    return out;
  }

  etapaDia(s, t, out) {
    const n = Number(t);
    const dias = s.dados.dias;
    if (!Number.isInteger(n) || n < 1 || n > dias.length) {
      out.respostas.push(`Por favor, digite um número de *1* a *${dias.length}*.`);
      return out;
    }
    const data = dias[n - 1];
    const horas = agenda.horariosLivres(this.config, this.storage, data, this.agora());
    if (horas.length === 0) {
      out.respostas.push('Esse dia acabou de lotar. 😕 Escolha outro:');
      return this.mostrarDias(s, out);
    }
    s.dados.data = data;
    s.dados.horas = horas;
    s.etapa = ETAPA.HORA;
    const lista = horas.map((h, i) => `*${i + 1}* - ${h}`);
    out.respostas.push(
      `📅 ${agenda.formatarData(data)}\n\nEscolha o horário:\n\n${lista.join('\n')}\n\n` +
        '_Digite *voltar* para recomeçar._',
    );
    return out;
  }

  etapaHora(s, t, out) {
    const horas = s.dados.horas;
    const n = Number(t);
    let hora = Number.isInteger(n) && n >= 1 && n <= horas.length ? horas[n - 1] : null;
    if (!hora) {
      // aceita também "14:00", "14h", "14h30"
      const m = t.match(/^(\d{1,2})(?:[:h ](\d{2}))?h?$/);
      if (m && Number(m[1]) <= 23) {
        const cand = `${m[1].padStart(2, '0')}:${m[2] || '00'}`;
        if (horas.includes(cand)) hora = cand;
      }
    }
    if (!hora) {
      out.respostas.push(`Por favor, escolha um número de *1* a *${horas.length}*.`);
      return out;
    }
    s.dados.hora = hora;
    s.etapa = ETAPA.NOME;
    const sugestao = s.nomeContato ? `\n_(ou digite *1* para usar "${s.nomeContato}")_` : '';
    out.respostas.push(`⏰ ${hora} ✅\n\nPara finalizar, qual o *nome completo* do paciente?${sugestao}`);
    return out;
  }

  etapaNome(s, textoOriginal, out) {
    let nome = (textoOriginal || '').trim().replace(/\s+/g, ' ');
    if (nome === '1' && s.nomeContato) nome = s.nomeContato;
    const letras = (nome.match(/\p{L}/gu) || []).length;
    if (letras < 3 || nome.length > 80) {
      out.respostas.push('Por favor, digite o nome completo do paciente.');
      return out;
    }
    s.dados.nome = nome;
    s.etapa = ETAPA.CONFIRMAR;
    out.respostas.push(
      [
        '📋 *Confira os dados do agendamento:*',
        '',
        `👤 Paciente: ${nome}`,
        `🦷 Serviço: ${s.dados.servico}`,
        `📅 Data: ${agenda.formatarData(s.dados.data)}`,
        `⏰ Horário: ${s.dados.hora}`,
        '',
        'Está tudo certo?',
        '*1* - Sim, confirmar',
        '*2* - Não, recomeçar',
      ].join('\n'),
    );
    return out;
  }

  etapaConfirmar(s, t, out, chatId) {
    if (NAO.test(t)) {
      s.dados = {};
      out.respostas.push('Sem problemas, vamos recomeçar.');
      return this.executarOpcao('1', s, out, chatId);
    }
    if (!SIM.test(t)) {
      out.respostas.push('Digite *1* para confirmar ou *2* para recomeçar.');
      return out;
    }
    const { data, hora, servico, nome } = s.dados;
    // Verifica de novo: outra pessoa pode ter pego o horário nesse meio-tempo
    const livres = agenda.horariosLivres(this.config, this.storage, data, this.agora());
    if (!livres.includes(hora)) {
      out.respostas.push('Que pena, esse horário acabou de ser reservado por outra pessoa. 😕');
      return this.mostrarDias(s, out);
    }
    const ag = this.storage.criarAgendamento({ chatId, nome, servico, data, hora });
    s.etapa = ETAPA.MENU;
    s.dados = {};
    out.respostas.push(
      [
        '✅ *Agendamento confirmado!*',
        '',
        `📅 ${agenda.formatarData(data)} às ${hora}`,
        `🦷 ${servico}`,
        `📍 ${this.config.clinica.endereco}`,
        '',
        'Chegue com 10 minutos de antecedência e traga um documento com foto' +
          ' (e a carteirinha do convênio, se tiver).',
        this.config.lembreteVespera ? 'Vamos te enviar um lembrete na véspera. 😉' : null,
        '',
        'Para cancelar ou ver seus horários, digite *2*.',
      ]
        .filter((l) => l !== null)
        .join('\n'),
    );
    out.avisoAdmin =
      `📅 *Novo agendamento* #${ag.id}\n👤 ${nome} (${numeroDe(chatId)})\n🦷 ${servico}\n` +
      `🗓️ ${agenda.formatarData(data)} às ${hora}`;
    return out;
  }

  listarAgendamentos(s, out, chatId) {
    const hoje = agenda.agoraNoFuso(this.config.fusoHorario, this.agora()).data;
    const ags = this.storage.agendamentosAtivos(chatId, hoje);
    if (ags.length === 0) {
      s.etapa = ETAPA.MENU;
      out.respostas.push('Você não tem agendamentos futuros. 🗓️\nDigite *1* para agendar uma consulta.');
      return out;
    }
    s.dados.agendamentos = ags.map((a) => a.id);
    s.etapa = ETAPA.CANCELAR_ESCOLHER;
    const lista = ags.map(
      (a, i) => `*${i + 1}* - ${agenda.formatarDataCurta(a.data)} às ${a.hora} — ${a.servico} (${a.nome})`,
    );
    out.respostas.push(
      `🗓️ *Seus agendamentos:*\n\n${lista.join('\n')}\n\n` +
        'Para *cancelar*, digite o número do agendamento.\nOu digite *menu* para voltar.',
    );
    return out;
  }

  etapaCancelarEscolher(s, t, out) {
    const ids = s.dados.agendamentos;
    const n = Number(t);
    if (!Number.isInteger(n) || n < 1 || n > ids.length) {
      out.respostas.push(`Digite um número de *1* a *${ids.length}*, ou *menu* para voltar.`);
      return out;
    }
    s.dados.cancelarId = ids[n - 1];
    s.etapa = ETAPA.CANCELAR_CONFIRMAR;
    out.respostas.push('Tem certeza que deseja cancelar?\n*1* - Sim, cancelar\n*2* - Não, manter');
    return out;
  }

  etapaCancelarConfirmar(s, t, out, chatId) {
    if (NAO.test(t)) {
      s.etapa = ETAPA.MENU;
      s.dados = {};
      out.respostas.push('Ok, seu agendamento foi mantido. 👍' + this.rodape());
      return out;
    }
    if (!SIM.test(t)) {
      out.respostas.push('Digite *1* para cancelar ou *2* para manter.');
      return out;
    }
    const ag = this.storage.cancelar(s.dados.cancelarId);
    s.etapa = ETAPA.MENU;
    s.dados = {};
    out.respostas.push(
      `❌ Agendamento de ${agenda.formatarData(ag.data)} às ${ag.hora} cancelado.\n` +
        'Se quiser marcar um novo horário, digite *1*.',
    );
    out.avisoAdmin =
      `❌ *Cancelamento* #${ag.id}\n👤 ${ag.nome} (${numeroDe(chatId)})\n` +
      `🗓️ ${agenda.formatarData(ag.data)} às ${ag.hora} — ${ag.servico}`;
    return out;
  }

  // ------------------------------------------------------------- lembretes

  // Retorna a lista de lembretes a enviar agora: [{ chatId, texto, id }]
  lembretesPendentes() {
    if (!this.config.lembreteVespera) return [];
    const ref = agenda.agoraNoFuso(this.config.fusoHorario, this.agora());
    if (ref.minutos < agenda.paraMin(this.config.horaLembrete)) return [];
    const amanha = agenda.somarDias(ref.data, 1);
    return this.storage.paraLembrete(amanha).map((a) => ({
      id: a.id,
      chatId: a.chatId,
      texto:
        `Olá, ${a.nome.split(' ')[0]}! 😊 Passando para lembrar da sua consulta *amanhã*:\n\n` +
        `📅 ${agenda.formatarData(a.data)} às ${a.hora}\n🦷 ${a.servico}\n` +
        `📍 ${this.config.clinica.endereco}\n\n` +
        'Se não puder comparecer, digite *2* para cancelar. Até amanhã! 🦷',
    }));
  }
}

function numeroDe(chatId) {
  return String(chatId).split('@')[0];
}

module.exports = { Bot, ETAPA, normalizar };
