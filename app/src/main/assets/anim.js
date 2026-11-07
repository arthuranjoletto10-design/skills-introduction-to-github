// Motor de animação de demonstração (boneco + equipamento)
const ST={h:[100,28],s:[100,48],e:[100,76],w:[100,102],hp:[100,104],k:[100,140],a:[100,178]};
const P2=(o)=>Object.assign({},ST,o);
const POSES={
 squat:{eq:'bar',A:P2({e:[86,60],w:[96,46]}),B:{h:[96,70],s:[90,88],e:[76,98],w:[86,84],hp:[66,130],k:[114,136],a:[100,178]}},
 hinge:{eq:'bar',A:P2({w:[104,102]}),B:{h:[142,72],s:[128,74],e:[128,100],w:[128,128],hp:[84,104],k:[98,140],a:[100,178]}},
 bench:{eq:'bar',bench:1,A:{h:[152,104],s:[136,110],e:[136,84],w:[136,58],hp:[80,112],k:[52,122],a:[48,178]},B:{h:[152,104],s:[136,110],e:[118,100],w:[134,94],hp:[80,112],k:[52,122],a:[48,178]}},
 fly:{eq:'db',bench:1,A:{h:[152,104],s:[136,110],e:[138,84],w:[136,58],hp:[80,112],k:[52,122],a:[48,178]},B:{h:[152,104],s:[136,110],e:[150,96],w:[160,104],hp:[80,112],k:[52,122],a:[48,178]}},
 ohp:{eq:'bar',A:P2({e:[86,60],w:[98,46]}),B:P2({e:[102,26],w:[102,4]})},
 row:{eq:'bar',A:{h:[146,70],s:[132,74],e:[132,100],w:[132,126],hp:[86,104],k:[98,138],a:[100,178]},B:{h:[146,70],s:[132,74],e:[108,82],w:[124,100],hp:[86,104],k:[98,138],a:[100,178]}},
 pullup:{eq:'pull',A:{h:[100,48],s:[100,64],e:[100,38],w:[100,12],hp:[100,118],k:[106,150],a:[100,180]},B:{h:[104,24],s:[100,40],e:[86,48],w:[100,12],hp:[100,94],k:[106,126],a:[100,156]}},
 curl:{eq:'db',A:P2({w:[102,102]}),B:P2({w:[118,54]})},
 triceps:{eq:'cable',A:P2({w:[118,60]}),B:P2({w:[104,102]})},
 raise:{eq:'db',A:P2({w:[102,102]}),B:P2({e:[124,58],w:[148,52]})},
 lunge:{eq:'db',A:P2({}),B:{h:[100,54],s:[100,74],e:[100,100],w:[100,124],hp:[100,128],k:[128,142],a:[128,178],k2:[84,168],a2:[62,176]}},
 legext:{eq:'machine',A:{h:[90,46],s:[88,66],e:[98,92],w:[106,118],hp:[90,122],k:[126,124],a:[124,172]},B:{h:[90,46],s:[88,66],e:[98,92],w:[106,118],hp:[90,122],k:[126,124],a:[170,112]}},
 legcurl:{eq:'none',bench:2,A:{h:[38,104],s:[54,110],e:[46,126],w:[34,122],hp:[110,112],k:[150,112],a:[186,112]},B:{h:[38,104],s:[54,110],e:[46,126],w:[34,122],hp:[110,112],k:[150,112],a:[160,76]}},
 hipthrust:{eq:'bar',bench:3,A:{h:[46,110],s:[60,120],e:[80,140],w:[94,158],hp:[94,162],k:[132,132],a:[136,178]},B:{h:[48,108],s:[60,120],e:[80,124],w:[96,122],hp:[98,124],k:[134,128],a:[136,178]}},
 calf:{eq:'db',A:P2({}),B:{h:[100,16],s:[100,36],e:[100,64],w:[100,90],hp:[100,92],k:[100,128],a:[100,166]}},
 crunch:{eq:'none',A:{h:[40,160],s:[56,166],e:[52,150],w:[42,146],hp:[110,172],k:[140,142],a:[166,176]},B:{h:[72,128],s:[80,142],e:[78,124],w:[66,122],hp:[110,172],k:[140,142],a:[166,176]}},
 plank:{eq:'none',A:{h:[40,138],s:[56,146],e:[56,174],w:[40,176],hp:[110,152],k:[140,160],a:[172,172]},B:{h:[40,136],s:[56,146],e:[56,174],w:[40,176],hp:[110,146],k:[140,158],a:[172,172]}},
 pushup:{eq:'none',A:{h:[40,124],s:[56,132],e:[56,155],w:[56,178],hp:[110,146],k:[140,160],a:[174,172]},B:{h:[40,152],s:[56,160],e:[80,162],w:[56,178],hp:[110,164],k:[140,168],a:[174,174]}},
 legraise:{eq:'pull',A:{h:[100,30],s:[100,44],e:[100,26],w:[100,10],hp:[100,98],k:[100,134],a:[100,170]},B:{h:[98,30],s:[100,44],e:[100,26],w:[100,10],hp:[104,100],k:[138,96],a:[172,90]}},
 run:{eq:'none',A:P2({e:[114,72],w:[126,60],k:[120,136],a:[108,176],k2:[86,140],a2:[70,160],e2:[88,76],w2:[82,98]}),B:P2({e:[88,76],w:[82,98],k:[86,140],a:[70,160],k2:[120,136],a2:[108,176],e2:[114,72],w2:[126,60]})},
 jump:{eq:'bar',A:{h:[96,70],s:[90,88],e:[96,110],w:[104,126],hp:[66,130],k:[114,136],a:[100,178]},B:P2({e:[102,26],w:[102,4]})},
 shrug:{eq:'bar',A:P2({w:[104,102]}),B:P2({h:[100,22],s:[100,40],e:[100,68],w:[104,94]})},
 leg:{eq:'none',A:P2({w:[92,98]}),B:P2({w:[92,98],k2:[124,132],a2:[144,166]})},
 stretch:{eq:'none',A:P2({e:[100,26],w:[100,4]}),B:{h:[138,96],s:[124,88],e:[134,120],w:[130,150],hp:[90,104],k:[98,140],a:[100,178]}}
};
const DICAS={
 squat:['Pés na largura dos ombros, pontas levemente para fora.','Desça levando o quadril para trás e para baixo, peito aberto.','Joelhos acompanham a direção dos pés; desça até ao menos a paralela.','Suba empurrando o chão com o pé inteiro, expirando.'],
 hinge:['Barra próxima às canelas, coluna neutra e abdômen firme.','Leve o quadril para trás mantendo a barra rente ao corpo.','Desça até sentir alongar o posterior, sem arredondar as costas.','Volte estendendo o quadril e contraindo glúteos no topo.'],
 bench:['Deite com olhos sob a barra, escápulas retraídas e pés firmes no chão.','Desça a carga controlada até a linha do peito.','Cotovelos a cerca de 45° do tronco.','Empurre de volta até estender os braços, expirando.'],
 fly:['Deite com os braços estendidos acima do peito, cotovelos levemente flexionados.','Abra os braços em arco, controlando a descida.','Sinta o alongamento do peitoral, sem passar da linha dos ombros.','Feche o movimento contraindo o peito, como num abraço.'],
 ohp:['Carga na altura dos ombros, punhos firmes e abdômen contraído.','Empurre para cima em linha reta, passando próximo ao rosto.','Estenda completamente os braços sem arquear a lombar.','Desça controlando até a altura dos ombros.'],
 row:['Incline o tronco à frente com coluna neutra e joelhos semiflexionados.','Puxe a carga em direção ao abdômen levando os cotovelos para trás.','Aperte as escápulas no fim do movimento.','Retorne alongando as costas sem perder a postura.'],
 pullup:['Segure a barra/pegador com os braços estendidos.','Puxe levando o peito em direção à barra e cotovelos para baixo.','Contraia as costas no topo, sem balançar o corpo.','Desça devagar até estender os braços.'],
 curl:['Em pé, cotovelos colados ao tronco.','Flexione os cotovelos trazendo a carga para cima.','Contraia o bíceps no topo sem mover os ombros.','Desça de forma controlada até estender.'],
 triceps:['Cotovelos fixos próximos ao corpo.','Estenda os cotovelos empurrando a carga.','Contraia o tríceps com os braços totalmente estendidos.','Volte devagar sem mover os ombros.'],
 raise:['Em pé, carga ao lado do corpo, cotovelos levemente flexionados.','Eleve os braços até a altura dos ombros.','Evite usar impulso do tronco.','Desça controlando o movimento.'],
 lunge:['Em pé, dê um passo largo mantendo o tronco ereto.','Desça até os dois joelhos formarem cerca de 90°.','Joelho da frente alinhado com o pé.','Empurre com o calcanhar da frente para voltar.'],
 legext:['Ajuste o encosto para o joelho ficar alinhado ao eixo da máquina.','Estenda os joelhos até as pernas ficarem retas.','Segure 1 segundo contraindo o quadríceps.','Desça devagar sem deixar o peso bater.'],
 legcurl:['Posicione o rolo logo acima dos calcanhares.','Flexione os joelhos trazendo os calcanhares ao glúteo.','Mantenha o quadril apoiado, sem levantar.','Volte de forma controlada.'],
 hipthrust:['Apoie a parte alta das costas no banco, carga sobre o quadril.','Pés firmes, canelas verticais no topo.','Eleve o quadril até alinhar tronco e coxas, contraindo os glúteos.','Desça controlando sem perder a tensão.'],
 calf:['Apoie a ponta dos pés, calcanhares livres.','Suba o máximo possível nas pontas dos pés.','Segure a contração no topo por 1 segundo.','Desça alongando bem a panturrilha.'],
 crunch:['Deite com joelhos flexionados e lombar apoiada.','Enrole o tronco aproximando costelas e quadril.','Expire contraindo o abdômen, sem puxar o pescoço.','Volte devagar mantendo a tensão.'],
 plank:['Apoie antebraços e pontas dos pés.','Mantenha corpo alinhado da cabeça aos calcanhares.','Contraia abdômen e glúteos; não deixe o quadril cair.','Respire normalmente e mantenha o tempo planejado.'],
 pushup:['Mãos um pouco mais abertas que os ombros, corpo alinhado.','Desça o peito em direção ao chão com cotovelos a 45°.','Mantenha abdômen e glúteos contraídos.','Empurre o chão até estender os braços.'],
 legraise:['Pendure-se na barra com braços estendidos.','Eleve as pernas contraindo o abdômen, sem balançar.','Leve até a altura do quadril ou acima.','Desça devagar controlando.'],
 run:['Mantenha postura ereta e olhar à frente.','Braços acompanham o ritmo das pernas.','Controle a respiração e a intensidade.','Ajuste velocidade/carga conforme o objetivo.'],
 jump:['Comece em posição de agachamento/carga baixa, coluna neutra.','Estenda quadril, joelhos e tornozelos de forma explosiva.','Leve a carga/corpo para cima com velocidade.','Receba a carga ou aterrisse de forma controlada.'],
 shrug:['Em pé, carga ao lado ou à frente do corpo.','Eleve os ombros em direção às orelhas.','Segure 1 segundo no topo.','Desça devagar sem girar os ombros.'],
 leg:['Em pé ou apoiado, tronco estável.','Afaste/eleve a perna contraindo o glúteo ou adutor.','Evite compensar com o tronco.','Volte controlando e repita.'],
 stretch:['Entre na posição devagar, sem trancos.','Respire fundo e relaxe a musculatura alongada.','Mantenha de 20 a 40 segundos.','Saia da posição lentamente.']
};
const REGRAS=[
 [/pescoco|encolhimento/,'shrug'],
 [/esteira|bicicleta|bike|eliptico|escada|stair|remo ergo|skierg|pular corda|farmer|carry|sled|prowler|yoke|bear crawl|battle|corda naval|walk|pogo/,'run'],
 [/thruster|wall ball|arranco|snatch|clean|arremesso|jerk|box jump|salto|jump|slam|man maker|devil|get-up|high pull|atlas|tire|sledge|burpee|muscle snatch|kettlebell swing americano/,'jump'],
 [/panturrilha|ponta de pe|tibial/,'calf'],
 [/nordic|flexora|glute-ham|flexao de joelho/,'legcurl'],
 [/extensora/,'legext'],
 [/hip thrust|pelvica|ponte|frog|glute bridge|b-stance/,'hipthrust'],
 [/abducao|aducao|adutora|abdutora|clamshell|fire hydrant|coice no cabo|gluteo coice|gluteo na maquina|4 apoios|kickback com|monster|copenhagen/,'leg'],
 [/agach|leg press|hack|pistol|wall sit|cadeirinha|pendulum|squat/,'squat'],
 [/afundo|passada|bulgaro|step-up|subida|avanco|lunge/,'lunge'],
 [/terra|stiff|romeno|good morning|rack pull|swing|pull-through|deadlift|hiperextensao|superman/,'hinge'],
 [/crucifixo inverso|elevacao lateral|elevacao frontal|elevacao em y|lu raise|rotacao|cuban|bus driver|face pull/,'raise'],
 [/elevacao de pernas|elevacao de joelhos|toes to bar|windshield/,'legraise'],
 [/prancha|hollow|dead bug|bird dog|l-sit|stir|pallof|dragon|suitcase/,'plank'],
 [/abdominal|crunch|sit-up|russian|v-up|roda|rollout|flutter|reverse crunch|elevacao de quadril|mountain|inclinacao lateral|lenhador|landmine rotation/,'crunch'],
 [/flexao|push-up/,'pushup'],
 [/desenvolvimento|push press|z press|landmine press|handstand|overhead/,'ohp'],
 [/crucifixo|crossover|fly|voador|peck/,'fly'],
 [/supino|press|svend/,'bench'],
 [/remada|renegade/,'row'],
 [/barra fixa|puxada|pulldown|chin|muscle-up|gravitron|escalada|towel|pullover|dead hang/,'pullup'],
 [/triceps|mergulho|paralelas|coice|tate|rolling|extensao de triceps/,'triceps'],
 [/rosca|curl|punho|grip|pinca|desvio|pronacao/,'curl']
];
const GDEF={"Peito":"bench","Costas":"row","Ombros":"raise","Bíceps":"curl","Tríceps":"triceps","Antebraço":"curl","Quadríceps":"squat","Posterior de coxa":"hinge","Glúteos":"hipthrust","Adutores":"leg","Panturrilha":"calf","Abdômen e core":"crunch","Pescoço":"shrug","Olímpicos e potência":"jump","Funcional e strongman":"run","Cardio (máquinas)":"run","Alongamento e mobilidade":"stretch"};
function padrao(g,e){if(g=='Alongamento e mobilidade')return'stretch';if(g=='Cardio (máquinas)')return'run';
 const n=e.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g,'');for(const[r,p]of REGRAS)if(r.test(n))return p;return GDEF[g]||'curl'}
let animRaf=0;
function iniciaAnim(el,pat){
 cancelAnimationFrame(animRaf);const D=POSES[pat];
 const ease=t=>t<.5?4*t*t*t:1-Math.pow(-2*t+2,3)/2;
 const L=(a,b,t)=>[a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t];
 const ln=(p,q,w,c)=>`<line x1="${p[0]}" y1="${p[1]}" x2="${q[0]}" y2="${q[1]}" stroke="${c}" stroke-width="${w}" stroke-linecap="round"/>`;
 const t0=performance.now(),per=2600;
 function frame(now){
  if(!document.body.contains(el))return;
  const c=((now-t0)%per)/per;const tt=ease(c<.5?c*2:2-c*2);const p={};
  for(const k of new Set([...Object.keys(D.A),...Object.keys(D.B)])){const a=D.A[k]||D.A[k.replace('2','')],b=D.B[k]||D.B[k.replace('2','')]||a;p[k]=L(a,b,tt)}
  p.k2=p.k2||p.k;p.a2=p.a2||p.a;p.e2=p.e2||p.e;p.w2=p.w2||p.w;
  let s='';const F='#2b2b31',G='url(#gg)',B='#f4f1ea',B2='#9c9aa3';
  s+=`<line x1="0" y1="181" x2="200" y2="181" stroke="#2a2a30" stroke-width="2"/>`;
  if(D.bench==1)s+=`<rect x="60" y="116" width="104" height="9" rx="4" fill="${F}"/><rect x="74" y="125" width="6" height="56" fill="${F}"/><rect x="144" y="125" width="6" height="56" fill="${F}"/>`;
  if(D.bench==2)s+=`<rect x="24" y="118" width="150" height="9" rx="4" fill="${F}"/><rect x="40" y="127" width="6" height="54" fill="${F}"/><rect x="150" y="127" width="6" height="54" fill="${F}"/>`;
  if(D.bench==3)s+=`<rect x="20" y="124" width="40" height="57" rx="5" fill="${F}"/>`;
  if(D.eq=='machine')s+=`<rect x="70" y="124" width="40" height="8" rx="3" fill="${F}"/><rect x="70" y="60" width="8" height="70" rx="3" fill="${F}"/><rect x="86" y="132" width="6" height="49" fill="${F}"/><circle cx="${p.a[0]}" cy="${p.a[1]}" r="6" fill="${G}"/>`;
  if(D.eq=='pull')s+=`<line x1="40" y1="10" x2="160" y2="10" stroke="${F}" stroke-width="6" stroke-linecap="round"/><line x1="44" y1="10" x2="44" y2="181" stroke="${F}" stroke-width="5"/><line x1="156" y1="10" x2="156" y2="181" stroke="${F}" stroke-width="5"/>`;
  if(D.eq=='cable')s+=`<rect x="150" y="0" width="10" height="181" fill="${F}"/>`+ln([152,6],p.w,1.5,'#6b6970');
  // membros de trás
  s+=ln(p.hp,p.k2,9,B2)+ln(p.k2,p.a2,8,B2)+ln(p.a2,[p.a2[0]+13,Math.min(p.a2[1]+2,181)],6,B2);
  s+=ln(p.s,p.e2,7,B2)+ln(p.e2,p.w2,6,B2);
  // tronco e cabeça
  s+=ln(p.s,p.hp,14,B)+`<circle cx="${p.h[0]}" cy="${p.h[1]}" r="10" fill="${B}"/>`;
  // membros da frente
  s+=ln(p.hp,p.k,10,B)+ln(p.k,p.a,9,B)+ln(p.a,[p.a[0]+14,Math.min(p.a[1]+2,181)],7,B);
  s+=ln(p.s,p.e,8,B)+ln(p.e,p.w,7,B);
  if(D.eq=='bar')s+=`<rect x="${p.w[0]-11}" y="${p.w[1]-11}" width="22" height="22" rx="4" fill="${G}" opacity=".95"/><circle cx="${p.w[0]}" cy="${p.w[1]}" r="3" fill="#1a1306"/>`;
  if(D.eq=='db')s+=`<rect x="${p.w[0]-9}" y="${p.w[1]-4}" width="18" height="8" rx="3" fill="${G}"/><rect x="${p.w2[0]-9}" y="${p.w2[1]-4}" width="18" height="8" rx="3" fill="${G}" opacity=".5"/>`;
  if(D.eq=='cable')s+=`<circle cx="${p.w[0]}" cy="${p.w[1]}" r="4" fill="${G}"/>`;
  el.innerHTML=`<defs><linearGradient id="gg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#f5d68a"/><stop offset="1" stop-color="#8a6424"/></linearGradient></defs>`+s;
  animRaf=requestAnimationFrame(frame)}
 animRaf=requestAnimationFrame(frame)}
