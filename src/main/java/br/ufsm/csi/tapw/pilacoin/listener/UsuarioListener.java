package br.ufsm.csi.tapw.pilacoin.listener;

import br.ufsm.csi.tapw.pilacoin.service.RabbitMQService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsuarioListener {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioListener.class);

    private final RabbitMQService rabbitMQService;

    @Autowired
    public UsuarioListener(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

    @RabbitListener(queues = "augusto")
    public void handleAugustoMessage(String mensagem) {
        // Lógica para processar mensagens da fila "augusto"
        logger.info("[handleAugustoMessage] Recebido da fila 'augusto': {}", mensagem);
    }

    @RabbitListener(queues = "Augusto-pila-validado")
    public void handlePilaValidadoMessage(String mensagem) {
        // Lógica para processar mensagens da fila "augusto-pila-validado"
        logger.info("[handlePilaValidadoMessage] Recebido da fila 'augusto-pila-validado': {}", mensagem);
    }

    @RabbitListener(queues = "Augusto-bloco-validado")
    public void handleBlocoValidadoMessage(String mensagem) {
        // Lógica para processar mensagens da fila "augusto-bloco-validado"
        logger.info("[handleBlocoValidadoMessage] Recebido da fila 'augusto-bloco-validado': {}", mensagem);
    }
}
