package br.ufsm.csi.tapw.pilacoin.listener;

import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinJson;
import br.ufsm.csi.tapw.pilacoin.service.RabbitMQService;
import br.ufsm.csi.tapw.pilacoin.service.ValidarPilacoinService;
import br.ufsm.csi.tapw.pilacoin.util.PilacoinDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PilacoinMineradoListener {
    private static final Logger logger = LoggerFactory.getLogger(PilacoinMineradoListener.class);

    private final RabbitMQService rabbitMQService;
    private final ValidarPilacoinService validarPilacoinService;
    private final PilacoinDataHandler pilacoinDataHandler;

    @Autowired
    public PilacoinMineradoListener(RabbitMQService rabbitMQService, ValidarPilacoinService validarPilacoinService, PilacoinDataHandler pilacoinDataHandler) {
        this.rabbitMQService = rabbitMQService;
        this.validarPilacoinService = validarPilacoinService;
        this.pilacoinDataHandler = pilacoinDataHandler;
    }

    @RabbitListener(queues = "pila-minerado")
    public void handlePilaMineradoMessage(String mensagem) {
        try {
            // Chama o método de validação na classe ValidarPilacoinService
            boolean validado = validarPilacoinService.processarPilacoin(mensagem);

            if(!validado){
                //pilacoin NÃO foi validado. Reenviar para a fila.
                logger.warn("[handlePilaMineradoMessage] pilacoin NÃO foi validado. Reenviando para a fila.");
                rabbitMQService.enviarMensagemParaFila("pila-minerado", mensagem);
            }else if (validado){
                //pilacoin foi validado com sucesso. Tudo já foi feito na classe ValidarPilacoinService.
                logger.info("[handlePilaMineradoMessage] Pilacoin Minerado foi validado com sucesso por Augusto.");
            }
        } catch (Exception e) {
            // Em caso de erro, republica na fila "pila-minerado"
            logger.error("[handlePilaMineradoMessage] Fila pila-minerado Erro ao processar PilaCoin. Republicando na fila 'pila-minerado'.", e);
            rabbitMQService.enviarMensagemParaFila("pila-minerado", mensagem);
        }
    }
}
