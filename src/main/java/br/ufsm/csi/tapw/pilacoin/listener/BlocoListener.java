package br.ufsm.csi.tapw.pilacoin.listener;

import br.ufsm.csi.tapw.pilacoin.service.MinerarBlocoService;
import br.ufsm.csi.tapw.pilacoin.service.RabbitMQService;
import br.ufsm.csi.tapw.pilacoin.service.ValidarBlocoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class BlocoListener {

    private static final Logger logger = LoggerFactory.getLogger(BlocoListener.class);

    private final RabbitMQService rabbitMQService;
    private final MinerarBlocoService minerarBlocoService;
    private final ValidarBlocoService validarBlocoService;

    @Autowired
    public BlocoListener(RabbitMQService rabbitMQService, MinerarBlocoService minerarBlocoService, ValidarBlocoService validarBlocoService) {
        this.rabbitMQService = rabbitMQService;
        this.minerarBlocoService = minerarBlocoService;
        this.validarBlocoService = validarBlocoService;
    }

    @RabbitListener(queues = "descobre-bloco")
    public void handleDescobreblocoMessage(String mensagem) {
        logger.info("[handleDescobreblocoMessage] Recebido da fila 'descobre-bloco': {}", mensagem);
        CompletableFuture<Void> resultadoMineracao = minerarBlocoService.minerarBlocoAsync(mensagem);
    }

    @RabbitListener(queues = "bloco-minerado")
    public void handleBlocoMineradoMessage(String mensagem) {
        logger.info("[handleBlocoMineradoMessage] Recebido da fila 'bloco-minerado': {}", mensagem);
        CompletableFuture<Void> resultadoValidacao = validarBlocoService.validarBlocoAsync(mensagem);
    }
}
