package br.ufsm.csi.tapw.pilacoin.listener;

import br.ufsm.csi.tapw.pilacoin.service.RabbitMQService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Service
public class TransacaoListener {

    private static final Logger logger = LoggerFactory.getLogger(TransacaoListener.class);

    private final RabbitMQService rabbitMQService;

    @Autowired
    public TransacaoListener(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

    /*@RabbitListener(queues = "descobre-bloco")
    public void handleDescobreblocoMessage(String mensagem) {
        // Lógica para processar mensagens da fila "descobre-bloco"
        logger.info("[handleDescobreblocoMessage] Recebido da fila 'descobre-bloco': {}", mensagem);
    }*/
}
