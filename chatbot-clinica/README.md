# 🦷 Chatbot para Clínica Odontológica (WhatsApp, sem API paga)

Robô de atendimento que funciona **dentro do próprio WhatsApp da clínica**, do mesmo
jeito que o WhatsApp Web: você escaneia um QR Code com o celular e o robô passa a
responder os pacientes automaticamente. **Não precisa contratar a API oficial nem
nenhum serviço pago.**

Ele usa a biblioteca gratuita [whatsapp-web.js](https://github.com/pedroslopez/whatsapp-web.js),
que controla um WhatsApp Web em segundo plano.

## O que o robô faz

| Opção | Função |
|---|---|
| 1️⃣ Agendar consulta | Paciente escolhe serviço → dia → horário livre → nome → confirma |
| 2️⃣ Meus agendamentos | Lista os horários do paciente e permite cancelar |
| 3️⃣ Serviços e valores | Tabela de serviços configurável |
| 4️⃣ Endereço e horários | Endereço, link do mapa e horário de funcionamento |
| 5️⃣ Convênios | Convênios aceitos e formas de pagamento |
| 6️⃣ Urgência | Mostra o contato de plantão e avisa a equipe |
| 7️⃣ Atendente | Passa a conversa para um humano (o robô fica em silêncio) |

Também:

- **Entende palavras**, não só números ("quero marcar", "quanto custa", "tô com dor"...).
- **Não marca dois pacientes no mesmo horário**, respeita almoço, feriados,
  sábado com meio período e antecedência mínima.
- **Lembrete automático na véspera** da consulta.
- **Avisa a clínica** (num número que você escolher) sobre novos agendamentos,
  cancelamentos, urgências e pedidos de atendente.
- **Atendimento humano sem conflito**: se alguém da clínica responder pelo celular,
  o robô para de responder naquela conversa por 12 horas (configurável).
- Ignora grupos e status.

## Como instalar

Você precisa de um computador (ou servidor) que fique ligado enquanto o robô
estiver em uso, com **[Node.js 18 ou mais novo](https://nodejs.org)** instalado.

```bash
cd chatbot-clinica
npm install
```

### 1. Configure a clínica

Abra `src/config.js` e preencha: nome, endereço, telefone, horários, serviços,
preços, convênios, feriados e o `numeroAdmin` (o WhatsApp que recebe os avisos,
no formato `5511999999999`).

### 2. Teste sem WhatsApp (opcional)

```bash
npm run simular
```

Converse com o robô no próprio terminal para ver se as mensagens estão do seu jeito.

### 3. Ligue o robô

```bash
npm start
```

Vai aparecer um **QR Code** no terminal. No celular da clínica, abra o WhatsApp →
**Aparelhos conectados** → **Conectar um aparelho** e escaneie. Pronto, o robô
está no ar. O login fica salvo na pasta `.wwebjs_auth`, então nas próximas vezes
não precisa escanear de novo.

> 💡 Funciona com WhatsApp comum e **WhatsApp Business**. O celular pode continuar
> sendo usado normalmente pela recepção.

## Comandos para a equipe (digite no celular da clínica, dentro da conversa)

| Comando | O que faz |
|---|---|
| `#bot` | Devolve a conversa para o robô (a mensagem é apagada automaticamente) |
| `#pausa` | Silencia o robô nessa conversa |
| *(qualquer resposta manual)* | O robô entende que um atendente assumiu e fica em silêncio |

O paciente também pode digitar **menu** a qualquer momento para voltar ao robô.

## Onde ficam os agendamentos

Em `dados/agendamentos.json` (arquivo simples, criado automaticamente). Faça cópia
de segurança dele de vez em quando.

## Deixar rodando 24h

Para manter o robô ligado mesmo após fechar o terminal ou reiniciar o computador,
use o [PM2](https://pm2.keymetrics.io/):

```bash
npm install -g pm2
pm2 start src/index.js --name clinica-bot
pm2 save
pm2 startup   # siga a instrução que aparecer
```

Num servidor Linux, pode ser preciso instalar as bibliotecas do Chrome; se o
robô não abrir, instale o pacote `chromium` da sua distribuição.

## ⚠️ Avisos importantes

- Essa forma de conexão **não é oficial**: o WhatsApp não autoriza robôs fora da API
  oficial e pode **bloquear o número** se detectar uso abusivo. O risco é baixo para
  um robô que **só responde** pacientes que mandaram mensagem (que é o caso aqui),
  mas existe. **Nunca use para disparar mensagens em massa.**
- Recomendo usar um número exclusivo da clínica e manter o WhatsApp do celular
  atualizado.
- Se o WhatsApp mudar o WhatsApp Web, pode ser preciso atualizar a biblioteca:
  `npm update whatsapp-web.js`.
- Os dados dos pacientes ficam no seu computador. Proteja o acesso a ele (LGPD).

## Estrutura

```
chatbot-clinica/
├── src/
│   ├── config.js      ← dados da clínica (edite aqui)
│   ├── index.js       ← conexão com o WhatsApp
│   ├── bot.js         ← fluxo da conversa
│   ├── agenda.js      ← cálculo de dias/horários livres
│   ├── storage.js     ← salva agendamentos em JSON
│   └── simulador.js   ← teste no terminal
├── test/              ← testes automáticos (npm test)
└── dados/             ← agendamentos (criado automaticamente)
```
