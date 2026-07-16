/*
 * Service worker minimo — existe para o navegador oferecer "Instalar" e o
 * sistema abrir como aplicativo de desktop.
 *
 * REGRA IMPORTANTE: este e um sistema autenticado, onde cada usuario ve dados
 * diferentes. Paginas HTML e chamadas de API NUNCA sao cacheadas: se fossem, o
 * navegador poderia mostrar a tela de um usuario para outro depois da troca de
 * login, ou exibir chamados ja alterados. So entram no cache arquivos estaticos
 * e publicos (CSS e icones), que sao iguais para todo mundo.
 */

const CACHE = 'chamados-estaticos-v1';
const ESTATICOS = [
    '/css/estilo.css',
    '/img/icone-192.png',
    '/img/icone-512.png'
];

self.addEventListener('install', evento => {
    evento.waitUntil(
        caches.open(CACHE)
            .then(cache => cache.addAll(ESTATICOS))
            .then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', evento => {
    // Remove caches de versoes anteriores para nao servir CSS velho apos um deploy.
    evento.waitUntil(
        caches.keys()
            .then(chaves => Promise.all(
                chaves.filter(chave => chave !== CACHE).map(chave => caches.delete(chave))
            ))
            .then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', evento => {
    const requisicao = evento.request;

    // Qualquer coisa que nao seja GET (login, formularios, exclusoes) vai
    // direto para a rede, sempre.
    if (requisicao.method !== 'GET') {
        return;
    }

    const url = new URL(requisicao.url);

    // Nao interferimos em outros dominios.
    if (url.origin !== self.location.origin) {
        return;
    }

    const ehEstatico = url.pathname.startsWith('/css/')
        || url.pathname.startsWith('/img/')
        || url.pathname.startsWith('/js/');

    if (!ehEstatico) {
        // Paginas do sistema: sempre da rede, nunca do cache.
        return;
    }

    // Estaticos: entrega do cache e atualiza em segundo plano.
    evento.respondWith(
        caches.match(requisicao).then(cacheado => {
            const daRede = fetch(requisicao).then(resposta => {
                if (resposta.ok) {
                    const copia = resposta.clone();
                    caches.open(CACHE).then(cache => cache.put(requisicao, copia));
                }
                return resposta;
            });
            return cacheado || daRede;
        })
    );
});
