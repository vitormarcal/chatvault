# Plano de exportação estática e encerramento do ChatVault

Status: implementado como exportação assíncrona acionada pela interface web. Instruções operacionais em [static-export.md](static-export.md). A validação com o acervo real continua necessária antes do encerramento.

## Objetivo

Exportar todo o acervo existente para um site HTML estático, legível offline ao abrir `index.html`, sem ChatVault, PostgreSQL, servidor HTTP ou acesso à internet. Preservar também os dados estruturados e os arquivos originais para futuras migrações. A entrega permite desativar e arquivar o serviço após validação humana.

Não corrigir a aplicação atual, reimportar arquivos, deduplicar mensagens nem alterar o banco durante a exportação. Duplicatas e inconsistências existentes devem ser preservadas e informadas.

## Decisões propostas

- Serviço Kotlin no backend, acionado por POST na interface web. Uma execução em segundo plano por instância; status e download por GET. Leitura via JDBC somente leitura, sem usar a deduplicação ou os serviços legados de exportação.
- Usar a conexão e as raízes já configuradas no serviço. Não registrar senha nem incluí-la nos artefatos.
- Usar transação PostgreSQL somente leitura com snapshot consistente. Para consistência entre banco e disco, bloquear alterações HTTP e aguardar o término da exportação no importador de e-mail, mantendo consultas disponíveis. O bloqueio vale para uma instância; alterações externas e outras instâncias precisam estar paradas.
- Exportar para diretório novo, fora das raízes de origem, sem sobrescrever saída anterior. Marcar saída incompleta e só publicar o marcador de conclusão após verificações. Falhas fatais devem produzir status de erro e saída identificável como incompleta.
- ZIP disponível para download após gerar e verificar o diretório navegável. Exibir necessidade de espaço: diretório e ZIP coexistentes podem exigir aproximadamente duas cópias do acervo, além da origem.
- HTML pré-gerado com CSS local e links relativos; nenhuma dependência de `fetch`, módulos JavaScript, CDN ou serviço externo. JavaScript opcional apenas para conveniências. Funcionalidade básica deve funcionar sem scripts.
- Paginação cronológica em lotes, 500 mensagens por página. Cursor JDBC ordenado por `(created_at, id)`; manter o ID original e âncoras para mensagens. Não usar deduplicação nem paginação do calendário atual.
- Datas preservadas como datas locais: o modelo atual usa `LocalDateTime` e não permite inferir fuso original com segurança. Não acrescentar conversão de fuso.
- JSON por conversa, escrito por streaming, contendo IDs, campos originais e referências aos arquivos exportados. Não depender de carregar todo o acervo em memória.

## Estrutura do artefato

```text
chatvault-export/
  index.html
  assets/styles.css
  chats/chat-<id>/
    index.html
    page-2.html
    messages.json
  files/file-<numero>.<extensao-segura>
  files.html
  data/chats.json
  manifest.json
  report.html
  README.txt
```

IDs e nomes gerados determinam os caminhos; nomes de conversas são apenas rótulos. Manter o mapeamento entre caminhos de origem relativos e exportados no manifesto, inclusive nomes originais, sem divulgar caminhos absolutos da máquina.

## Conteúdo e navegação

- Página inicial com todas as conversas, contagem de mensagens, período e foto de perfil quando existente; incluir conversas vazias.
- Mensagens com autor, data, texto multilinha e distinção de mensagens de sistema. Escapar conteúdo e atributos HTML, inclusive nomes e legendas.
- Links para início, página anterior/próxima e índice por datas que permita alcançar todos os dias, inclusive quando um dia ocupa várias páginas.
- Imagens comuns com carregamento tardio; áudio e vídeo com controles nativos e sem reprodução automática. Todo anexo tem link de download, mesmo quando não puder ser exibido pelo navegador.
- SVG, HTML e outros formatos ativos como download, sem incorporação como conteúdo da página. Considerar extensões seguras para cópias navegáveis e preservar nome original no manifesto. Navegadores podem não reproduzir todos os codecs: o arquivo continua disponível.
- Anexos ausentes aparecem explicitamente na mensagem e no relatório, sem interromper a leitura do restante.
- Interface responsiva em português, inspirada no chat atual: tema escuro, balões, lista lateral e galeria por conversa. Scripts locais opcionais oferecem busca por nome, seleção do próprio autor e pré-visualização; os links básicos continuam funcionais sem JavaScript.
- Busca global offline fica fora da primeira versão. A navegação por conversas/datas e a busca do navegador na página atendem à primeira entrega; documentar que a busca do navegador não pesquisa outras páginas.

## Preservação e integridade

- Ler todas as linhas de `chat` e `message`, sem usar `findAllChatsWithLastMessage`, que exige uma mensagem associada e pode omitir conversas vazias.
- Detectar mensagens sem conversa correspondente; preservá-las no JSON e em uma seção de recuperação, com aviso no relatório.
- Inventariar todos os arquivos regulares sob a raiz de arquivo, inclusive fotos de perfil, TXT originais e anexos não referenciados. Copiar cada caminho de origem uma vez; mensagens podem compartilhar o mesmo arquivo.
- Não seguir links simbólicos para fora da raiz; reportar links, caminhos inválidos, arquivos ilegíveis e tipos especiais. Não tratar esses itens como exportados com sucesso.
- Manter nomes seguros, extensões e mapeamento que evitem colisões, travessia de diretórios e problemas com acentos, espaços, `#` e `%` em URLs.
- Calcular tamanho e SHA-256 dos arquivos copiados. Manifesto lista arquivos, contagens, avisos, versão do exportador, versão do formato e horário da execução.
- Verificação final confronta mensagens por conversa e total com o snapshot, confirma JSON e HTML com os mesmos IDs e valida destinos dos links locais. Conferir arquivos copiados por leitura e hash antes de declarar integridade verificada.
- Distinguir: exportação íntegra; concluída com pendências conhecidas; falha/incompleta. Anexo ausente pode permitir conclusão com pendências, mas nunca uma declaração de integridade total.
- Raiz de importação pendente e diretório de exportações antigas não fazem parte do acervo principal. Documentar backup separado desses diretórios, se contiverem arquivos ainda necessários.

## Etapas de implementação

1. **Leitor e processamento:** serviço assíncrono, conexão somente leitura, cursor JDBC, snapshot e bloqueio de alterações/destinos perigosos. Validar nomes físicos de colunas gerados pelo Hibernate no esquema real.
2. **Preservação:** exportar JSON, inventariar e copiar arquivos, resolver anexos, produzir manifesto e estados de conclusão. Medir contagens antes de construir a apresentação.
3. **Site:** gerar índice, conversas paginadas, índice por datas, mídias e relatório com links locais seguros.
4. **Verificação e pacote:** validar contagens, IDs, hashes e links; gerar ZIP para download sem nomes temporários compartilhados e sem confundir arquivo incompleto com entrega final.
5. **Operação:** documentar execução no host e acesso aos volumes de uma instalação Docker, exemplos sem credenciais reais e procedimento de recuperação em caso de falha.
6. **Exportação real:** após conhecer conexão e localização dos dados, executar em uma única instância, sem modificações externas, revisar relatório e conferir conversas no navegador. Não encerrar nem arquivar automaticamente o serviço como parte do comando.

## Testes necessários

- Integração com PostgreSQL: conversas vazias, mensagens de sistema, mesmo timestamp com IDs diferentes, duplicatas preservadas e mensagens sem conversa quando o esquema permitir.
- Acervo maior que uma página e dia maior que uma página: nenhum ID omitido ou repetido; JSON e HTML concordam.
- Anexos presentes/ausentes, arquivos compartilhados, arquivos sem referência e fotos de perfil; diretórios aninhados e colisões de nomes.
- Texto com tags, aspas, acentos e múltiplas linhas; nomes com caracteres especiais; caminhos e links simbólicos que escapem da origem.
- Falta de espaço, arquivo sem permissão e interrupção: origem intacta e saída claramente incompleta.
- Abrir o resultado extraído via `file://` em navegador, offline, para verificar navegação, imagens e downloads; conferir fallback para mídia não suportada.
- Conferir manualmente amostras de conversas curtas e longas, início/fim do histórico e diferentes tipos de anexos.

## Critérios para encerrar o serviço

1. Guardar backup bruto do PostgreSQL e da raiz de arquivos, além de configuração necessária à recuperação, em local protegido.
2. Gerar exportação e revisar todas as pendências; não assumir que o exportador recupera dados já ausentes.
3. Conferir contagens, hashes e amostras no navegador; guardar uma segunda cópia independente do artefato.
4. Registrar onde estão as cópias e instruções para abrir o site e restaurar o backup bruto.
5. Somente após aceite do usuário, desativar a instalação e arquivar o repositório. Exclusão dos dados originais não faz parte deste plano.

## Informações necessárias antes da execução real

- Instalação efetiva: Docker ou processo local; acesso ao PostgreSQL e à raiz `chatvault.bucket.root`.
- Volume aproximado do acervo e espaço disponível no destino.
- Possibilidade de parar o serviço durante a cópia e local para guardar duas cópias do resultado.

Essas informações não bloqueiam a implementação com dados sintéticos. Credenciais reais não devem ser gravadas no repositório nem neste documento.
