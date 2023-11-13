package br.ufsm.csi.tapw.pilacoin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQService.class);
    private final AmqpTemplate amqpTemplate;
    @Autowired
    public RabbitMQService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void enviarMensagemParaFila(String nomeFila, String mensagem) {
        logger.info("[enviarMensagemParaFila] Mensagem enviada fila: {}, mensagem: {}", nomeFila, mensagem);
        amqpTemplate.convertAndSend(nomeFila, mensagem);
    }
}
