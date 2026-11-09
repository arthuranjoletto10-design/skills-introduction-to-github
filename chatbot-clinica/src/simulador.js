// Simulador no terminal: teste o robô sem conectar no WhatsApp.
// Uso: npm run simular

const readline = require('readline');
const path = require('path');
const config = require('./config');
const Storage = require('./storage');
const { Bot } = require('./bot');

const storage = new Storage(path.join(__dirname, '..', 'dados', 'simulador.json'));
const bot = new Bot({ config, storage });
const chatId = '5500000000000@c.us';

const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
console.log('=== Simulador do robô (digite "/fim" para sair) ===\n');

function perguntar() {
  rl.question('\n👤 Você: ', (texto) => {
    if (texto.trim() === '/fim') return rl.close();
    const { respostas, avisoAdmin } = bot.processar(chatId, texto, 'Paciente Teste');
    for (const r of respostas) console.log(`\n🤖 Robô:\n${r}`);
    if (avisoAdmin) console.log(`\n📣 [Aviso para a clínica]\n${avisoAdmin}`);
    perguntar();
  });
}
perguntar();
