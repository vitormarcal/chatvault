# Exportar o acervo como site HTML

A exportação fica na interface em **Exportar conversas → Exportar acervo como site HTML → Gerar exportação HTML**. O link legado de exportação continua disponível no mesmo diálogo.

## Uso

1. Execute a nova imagem com o mesmo PostgreSQL e volume de arquivos da instalação atual.
2. Confira espaço livre em `chatvault.bucket.export`: durante a geração, o site e o ZIP coexistem. Reserve pelo menos duas vezes o tamanho dos arquivos de origem, mais espaço para HTML/JSON. Arquivos de mídia podem compactar muito pouco.
3. Mantenha apenas uma instância do ChatVault e interrompa alterações externas no banco e nos arquivos. A aplicação bloqueia alterações HTTP e adia o processamento de e-mails enquanto exporta; consultas continuam disponíveis. Uma alteração já em andamento faz a solicitação retornar conflito: aguarde e tente novamente.
4. Acione a geração. Você pode fechar a página e retornar ao diálogo depois: o processamento ocorre no servidor. A interface apresenta fase, contagem de arquivos e progresso das mensagens.
5. Quando terminar, baixe o ZIP e **extraia-o completamente**. Abra `chatvault-export/index.html` no navegador. Não é necessário iniciar servidor, instalar dependências ou ter internet.
6. Confira `report.html`: avisos indicam anexos ausentes, arquivos não copiados, links simbólicos ou mensagens sem conversa correspondente. Uma exportação com pendências não equivale a um backup integral verificado.

## O que é preservado

- Todas as conversas, inclusive vazias; todas as mensagens, sem deduplicação, ordenadas por data e ID.
- Interface de chat em tema escuro, com lista lateral de conversas, balões, cores por autor, separadores de dia e fotos de perfil.
- HTML dividido em páginas de 500 mensagens, com navegação anterior/próxima e índice de datas. Um mesmo dia pode ocupar várias páginas.
- Galeria por conversa, com miniaturas, filtros de imagens/vídeos/áudios/documentos e páginas de 60 anexos. Os filtros abrangem a conversa inteira, não apenas a página atual de mensagens. Cada anexo tem um link para sua mensagem de origem.
- JSON por conversa com os campos originais das mensagens, IDs e referência ao arquivo copiado; `data/chats.json` contém metadados das conversas.
- Arquivos regulares de toda a raiz `chatvault.bucket.root`, inclusive originais sem vínculo no banco e fotos de perfil. `files.html` apresenta o inventário navegável.
- Mensagens sem conversa correspondente em uma conversa de recuperação identificada pelo ID original.
- Manifesto com tamanhos, hashes SHA-256, contagens, estado e avisos. Contagens do banco são confrontadas com JSON e IDs do HTML; links locais e hashes dos arquivos copiados são verificados antes da disponibilização.

As datas locais são preservadas sem inventar um fuso horário. O conteúdo das mensagens é escapado como texto. O site não usa recursos externos. Dois scripts locais carregam a lista de conversas e adicionam busca por nome, seleção de participante e pré-visualização de imagens/vídeos. Não há requisições de API ou `fetch`: os scripts funcionam via `file://`. A navegação por links, a galeria e os downloads continuam disponíveis sem JavaScript. Formatos ativos ou desconhecidos são copiados como `.bin`, mantendo o nome original no manifesto e no atributo de download. Imagens comuns são exibidas; áudio e vídeo usam controles nativos, mas codecs não suportados precisam de aplicativo externo. Todo anexo copiado tem link para download.

Na conversa, use **Eu sou** para escolher seu participante e alinhar seus balões à direita. A escolha é guardada localmente quando o navegador permite armazenamento para arquivos locais; não altera os dados exportados. Nenhuma identidade é presumida.

Na galeria, clique em uma imagem ou vídeo para ampliar; use Anterior/Próximo ou as setas do teclado, e Escape para fechar. A pré-visualização percorre os itens da página aberta. Anexos ausentes continuam identificados na galeria e no relatório. O painel de prévias aparece ao lado das mensagens em telas largas; a galeria completa é acessível também no celular.

A busca lateral filtra nomes de conversas. A busca do navegador pesquisa somente a página aberta; não há busca global no conteúdo das mensagens nesta versão.

## Operação e retenção

Não há nova variável obrigatória. O serviço usa o datasource existente, `chatvault.bucket.root` como origem e `chatvault.bucket.export/static-archives` como destino. As duas raízes precisam ser independentes, com permissão de leitura na origem e escrita no destino.

Cada execução tem UUID próprio e mantém:

```text
<chatvault.bucket.export>/static-archives/
  latest.json
  <uuid>/
    site/
    archive.zip
```

Downloads não apagam o ZIP. A última execução pode ser recuperada pela interface após reinício. Uma execução interrompida é marcada como falha; não há retomada automática nem download de ZIP parcial. Gere uma nova exportação nesse caso.

As execuções anteriores e diretórios incompletos são mantidos para inspeção. Não há limpeza automática: depois de conferir e guardar as cópias, o administrador pode remover os diretórios de execuções antigas no destino. Não remova diretórios de uma execução ativa ou de um download em curso. A interface apresenta apenas a última execução; ela não é um gerenciador de histórico.

O bloqueio de alterações é local à instância Java. Não cobre SQL externo, alterações manuais no volume nem outra instância do serviço. Não rode múltiplas instâncias durante a exportação.

Os endpoints usam o mesmo controle de acesso da instalação existente; este recurso não introduz autenticação. O ZIP contém dados pessoais do acervo inteiro.

## API

- `POST /api/archive-exports`: inicia; responde `202` com a execução ou `409` se houver exportação/alteração ativa.
- `GET /api/archive-exports/latest`: estado atual/último; `204` se ainda não existir.
- `GET /api/archive-exports/{uuid}/download`: ZIP verificado; `404` se inexistente ou ainda não concluído.

Estados: `running`, `completed`, `completed_with_warnings`, `failed`. Falhas detalhadas ficam nos logs; a interface recebe mensagem sem credenciais ou caminhos internos.

## Antes de encerrar o serviço

Guarde também um dump PostgreSQL, a raiz de arquivos e a configuração necessária à recuperação, em local protegido. Diretórios de importação pendente e exportações antigas não fazem parte da origem deste exportador e precisam de cópia separada se forem necessários.

Confira contagens, pendências e amostras de conversas longas e curtas, com diferentes anexos. Guarde uma segunda cópia independente do ZIP. Só então desative a instalação e arquive o repositório. O exportador não exclui nem corrige os dados originais.

## Validação de desenvolvimento

```sh
cd backend
./gradlew test ktlintCheck
```

O teste de integração usa um PostgreSQL descartável, com usuário `postgres` e senha `export_test`, criando/removendo apenas schemas aleatórios:

```sh
CHATVAULT_TEST_POSTGRES_URL=jdbc:postgresql://127.0.0.1:5432/chatvault ./gradlew test --rerun-tasks
```

Não use uma base de produção para testes. Para o frontend, execute `npm ci` e `npm run generate` em `frontend/`.
