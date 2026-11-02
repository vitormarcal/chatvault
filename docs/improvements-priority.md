# Melhorias e pendências por ordem de relevância

Registro de 02/11/2026, referente à versão 1.21.0.

A ordem considera a intenção do usuário de preservar o acervo e encerrar/arquivar o ChatVault. É uma priorização de recomendações, não uma autorização para implementá-las. Se o serviço continuar em uso, os problemas de integridade do importador ganham prioridade. As classificações refletem a revisão e sua reavaliação nesta sessão; não representam uma nova auditoria.

## Ordem de relevância

| Ordem | Recomendação | Relevância e situação |
|---|---|---|
| 1 | Validar o acervo exportado e guardar backups independentes | Prioridade para o encerramento: conferir relatório, contagens, pendências e amostras com anexos; guardar duas cópias independentes do ZIP, além de dump PostgreSQL, raiz de arquivos e configuração de recuperação. A conferência exaustiva do acervo real não está registrada; os testes sintéticos não a substituem. |
| 2 | Proteger o acesso enquanto o serviço permanecer ativo | Não há autenticação na API. Se estiver exposta a pessoas não confiáveis, restringir acesso ou usar proxy autenticado é urgente. Em ambiente privado/controlado, a gravidade diminui. Rever também PostgreSQL/Adminer publicados e credenciais de exemplo. CORS não substitui autenticação. |
| 3 | Corrigir a deduplicação do importador | Falha confirmada: igualdade de Message inclui id; a mensagem importada tem id nulo e a persistida tem id. Pode duplicar mensagens em exportações sobrepostas. Comparar apenas texto também é insuficiente: preservar repetições legítimas, ordem e mensagens no mesmo minuto. Pendente no fluxo original. |
| 4 | Permitir completar históricos antigos e tornar importações consistentes | Mensagens inéditas anteriores à última salva podem ser descartadas. Lista sem novidades vira erro; arquivos do ZIP podem já ter sido escritos antes da falha. Tratar reimportação sem novidades como sucesso e definir aplicação/recuperação consistente de alterações. Pendente. |
| 5 | Isolar a inferência de datas por importação | Falha reproduzida: o parser compartilhado retém o último formato e influencia importações posteriores, podendo trocar dia e mês silenciosamente. Configuração explícita adequada de formato evita esse caminho; o Compose principal já a define. Pendente quando se usa inferência. |
| 6 | Limitar expansão e memória das importações ZIP | Cada entrada descomprimida é carregada inteira em memória, sem teto por entrada, quantidade ou total expandido. TXT também vira lista de mensagens. Usar streaming e limites. A relevância cresce com arquivos grandes e uploads não confiáveis. Há validação de caminhos; não foi demonstrado Zip Slip. Pendente no importador. |
| 7 | Corrigir a navegação por data e o salto para resultados de busca | A interface pode pular o restante de um dia ao avançar diretamente para o seguinte; o salto por data pode não incluir a mensagem escolhida. Não apaga dados. Usar cursor por data e ID e contexto pelo ID. Pendente na interface original; o HTML estático usa paginação própria. |
| 8 | Isolar arquivos temporários na exportação legada | Exportações antigas da mesma conversa usam o mesmo ZIP e reescrevem o TXT da origem, permitindo conflitos concorrentes. Gravidade média, menor em uso individual. Pendente no fluxo legado; o exportador estático usa UUID próprio e impede execuções simultâneas. |
| 9 | Registrar resultados de importação e consistência entre banco e anexos | Histórico com contagens, falhas, arquivos rejeitados e reprocessamento ajuda a identificar alterações parciais. Pendente no importador. O novo exportador já gera relatório e inventário. |
| 10 | Tornar persistência e implantação explícitas | Declarar volume PostgreSQL, planejar recuperação, fixar versões das imagens e adicionar healthchecks. Ausência de volume declarado não demonstra perda a cada reinício; depends_on não verifica prontidão do banco. Melhoria operacional, não falha de perda de dados comprovada. |
| 11 | Adotar migrações versionadas do banco | Substituir ddl-auto=update conforme a necessidade de manter e atualizar o serviço com dados reais. Recomendação preventiva; não houve falha de migração demonstrada. |
| 12 | Corrigir e completar a busca da interface original | Passar parâmetros estruturados para preservar &, # e +; paginar ou sinalizar o limite de 50 resultados. Não foi demonstrada SQL injection: a consulta do repositório é parametrizada. Pendente. A busca lateral do HTML filtra apenas nomes de conversas. |
| 13 | Tratar respostas atrasadas e melhorar feedback na interface original | Cancelar/ignorar respostas de conversa ou consulta que deixou de ser atual; exibir progresso, resumo e erros claros. O risco de respostas atrasadas foi identificado no código, sem reprodução completa dos sintomas no navegador. Pendente nos fluxos antigos. O novo exportador tem progresso e status próprios. |
| 14 | Automatizar CI e ampliar testes de cenários reais | Priorizar PostgreSQL real, IDs persistidos, reimportações, sobreposição temporal e fluxos críticos do frontend. Os testes antigos de deduplicação usavam mensagens salvas simuladas sem ID. Foram adicionados testes do exportador, mas isso não corrige os testes/bugs do importador nem implementa CI. |
| 15 | Otimizar desempenho após medir | Avaliar índice por conversa/data/ID, busca textual, paginação de anexos e miniaturas. Não foi demonstrada lentidão com medições; evitar reformulação preventiva. A galeria estática já tem paginação e carregamento tardio, mas usa os arquivos originais, sem gerar miniaturas menores. |

## Entregue na versão 1.21.0

- Exportação assíncrona pela interface, ZIP com HTML offline, JSON por conversa e cópia dos arquivos, inclusive não vinculados a mensagens.
- Tema de chat, lista lateral, seleção de participante, balões, separadores de dia e galeria com filtros, paginação, ampliação e links para as mensagens.
- Relatório de pendências, verificação de contagens, IDs, links e hashes SHA-256.
- Diretórios exclusivos por execução, bloqueio temporário de alterações HTTP e espera no importador de e-mail; consultas continuam disponíveis.
- Estado recuperável após reinício; exportação interrompida exige nova execução. O bloqueio vale para uma instância, não para SQL externo ou alterações manuais no volume.
- 163 testes passaram com integração PostgreSQL; o fluxo offline foi conferido no Chrome com 1.321 mensagens e 135 anexos sintéticos, inclusive navegação básica sem JavaScript.

Essas entregas não corrigem retroativamente duplicatas, datas erradas, mensagens já descartadas ou anexos ausentes. O usuário relatou que o resultado inicialmente estava bom; não há registro de desativação ou arquivamento efetivo do serviço.

## Próxima ação recomendada

Concluir o item 1 e proteger o acesso conforme o item 2; então encerrar o serviço após conferência. Os itens 3 a 15 são principalmente um backlog caso o usuário decida continuar mantendo o ChatVault.

O guia operacional está em docs/static-export.md. O código foi enviado em bab9391 e a imagem 1.21.0 foi publicada também como latest.
