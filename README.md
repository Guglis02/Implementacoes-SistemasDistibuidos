# Causal-Multicast
Trabalho 3 da disciplina de Sistemas Distribuidos

## Como rodar
Atualmente o programa só permite troca de mensagem com exatos 3 participantes.
Então devem ser abertos três terminais com o comando:    
  
java Client port  
  
Por exemplo:  
java Client 1111  
java Client 2222  
java Client 3333  
  
  
## Buffer de mensagem
Existem dois buffers de mensagens, um no receptor, que armazena mensagens que não podem ser recebidas no momento por 
conta do vector clock (Elas são automaticamente recebidas no momento em que o vector clock ficar de acordo). O outro fica 
no emissor da mensagem, e armazena as mensagens que o emissor optou por não enviar, elas podem ser enviadas rodando o comando /sendDelayed
