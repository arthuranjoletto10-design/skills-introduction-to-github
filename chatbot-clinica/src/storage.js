// Armazenamento simples em arquivo JSON (sem banco de dados).
const fs = require('fs');
const path = require('path');

class Storage {
  constructor(arquivo = path.join(__dirname, '..', 'dados', 'agendamentos.json')) {
    this.arquivo = arquivo;
    this.dados = { agendamentos: [], proximoId: 1 };
    this.carregar();
  }

  carregar() {
    if (!this.arquivo) return; // modo memória (testes)
    try {
      if (fs.existsSync(this.arquivo)) {
        this.dados = JSON.parse(fs.readFileSync(this.arquivo, 'utf8'));
      }
    } catch (e) {
      console.error('Erro ao ler agendamentos, iniciando vazio:', e.message);
    }
  }

  salvar() {
    if (!this.arquivo) return;
    fs.mkdirSync(path.dirname(this.arquivo), { recursive: true });
    const tmp = this.arquivo + '.tmp';
    fs.writeFileSync(tmp, JSON.stringify(this.dados, null, 2));
    fs.renameSync(tmp, this.arquivo);
  }

  criarAgendamento({ chatId, nome, servico, data, hora }) {
    const ag = {
      id: this.dados.proximoId++,
      chatId,
      nome,
      servico,
      data, // AAAA-MM-DD
      hora, // HH:MM
      status: 'confirmado',
      criadoEm: new Date().toISOString(),
      lembreteEnviado: false,
    };
    this.dados.agendamentos.push(ag);
    this.salvar();
    return ag;
  }

  horarioOcupado(data, hora) {
    return this.dados.agendamentos.some(
      (a) => a.status === 'confirmado' && a.data === data && a.hora === hora,
    );
  }

  agendamentosAtivos(chatId, hoje) {
    return this.dados.agendamentos
      .filter((a) => a.chatId === chatId && a.status === 'confirmado' && a.data >= hoje)
      .sort((a, b) => (a.data + a.hora).localeCompare(b.data + b.hora));
  }

  cancelar(id) {
    const ag = this.dados.agendamentos.find((a) => a.id === id);
    if (!ag) return null;
    ag.status = 'cancelado';
    ag.canceladoEm = new Date().toISOString();
    this.salvar();
    return ag;
  }

  paraLembrete(data) {
    return this.dados.agendamentos.filter(
      (a) => a.status === 'confirmado' && a.data === data && !a.lembreteEnviado,
    );
  }

  marcarLembrete(id) {
    const ag = this.dados.agendamentos.find((a) => a.id === id);
    if (ag) {
      ag.lembreteEnviado = true;
      this.salvar();
    }
  }
}

module.exports = Storage;
