package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Dificuldade;
import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinValidadoJson;
import br.ufsm.csi.tapw.pilacoin.util.PilacoinDataHandler;
import br.ufsm.csi.tapw.pilacoin.util.RSAKeyPairGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Date;

@Service
public class ValidarPilacoinService {
    private static final Logger logger = LoggerFactory.getLogger(ValidarPilacoinService.class);
    private final RSAKeyPairGenerator rsaKeyPairGenerator;
    private final RabbitMQService rabbitMQService;
    private final PilacoinDataHandler pilacoinDataHandler;
    private final DificuldadeService dificuldadeService;

    @Autowired
    public ValidarPilacoinService(RSAKeyPairGenerator rsaKeyPairGenerator, PilacoinDataHandler pilacoinDataHandler, RabbitMQService rabbitMQService, DificuldadeService dificuldadeService) {
        this.rsaKeyPairGenerator = rsaKeyPairGenerator;
        this.pilacoinDataHandler = pilacoinDataHandler;
        this.rabbitMQService = rabbitMQService;
        this.dificuldadeService = dificuldadeService;
    }

    public boolean processarPilacoin(String strPilacoinJson) {
        try {
            logger.info("[processarPilacoin] Iniciando validação do Pilacoin: {}", strPilacoinJson);
            PilacoinJson pilacoinJson = pilacoinDataHandler.strParaObjPilacoinJson(strPilacoinJson);

            if ("Augusto".equals(pilacoinJson.getNomeCriador())) {
                // Se o criador é "Augusto", reenvia para a fila "pila-minerado"
                logger.info("[processarPilacoin] Pilacoin criado por Augusto. Ignorando validação. Retornando false para reenviar para a fila 'pila-minerado'.");
                return false;
            } else {
                // Se não for "Augusto", o Pilacoin deve ser validado
                byte[] hash = pilacoinDataHandler.getHash(strPilacoinJson); // gerando HASH da string original
                BigInteger hashBigInt = new BigInteger(hash).abs(); // Converter hash para BigInteger para comparar com a dificuldade
                Dificuldade ultimaDificuldade = dificuldadeService.getUltimaDificuldade();
                if (ultimaDificuldade == null || ultimaDificuldade.getValidadeFinal() == null){
                    logger.error("[processarPilacoin] Não pode validar Pilacoin -- ultimaDificuldade é null ou sem validade final..");
                    return false;
                }
                if (ultimaDificuldade.getValidadeFinal().compareTo(new Date()) > 0){
                    logger.error("[processarPilacoin] Não pode validar Pilacoin -- ultimaDificuldade está vencida.");
                    return false;
                }
                if (hashBigInt.compareTo(ultimaDificuldade.getDificuldade()) < 0) {
                    // Pilacoin validado com sucesso
                    // agora precisa assinar o pilacoin e enviar para a fila "pila-validado"
                    logger.info("[processarPilacoin] Pilacoin validado, gerando assinatura.");
                    byte[] assinaturaPilaCoin = pilacoinDataHandler.gerarAssinatura(pilacoinJson, rsaKeyPairGenerator.generateOrLoadKeyPair().getPrivate()); //assinatura do obj pilacoinJson
                    PilacoinValidadoJson pilacoinValidadoJson = new PilacoinValidadoJson("Augusto", rsaKeyPairGenerator.generateOrLoadKeyPair().getPublic().getEncoded(), assinaturaPilaCoin, pilacoinJson);
                    //logger.info("[processarPilacoin] Pilacoin assinado JSON: {}", pilacoinDataHandler.pilacoinValidadoJsonParaStrJson(pilacoinValidadoJson));
                    rabbitMQService.enviarMensagemParaFila("pila-validado", pilacoinDataHandler.pilacoinValidadoJsonParaStrJson(pilacoinValidadoJson));
                    return true;
                }
            }
            //log hash não é menor que a dificuldade atual
            logger.warn("[processarPilacoin] HASH não é menor que dificuldade...");
            return false;
        } catch (Exception e) {
            logger.error("[processarPilacoin] Erro ao processar Pilacoin: {}", e.getMessage());
            // Em caso de erro, trata de alguma forma (log, lançar exceção, etc.)
            // Pode ser necessário ajustar conforme sua necessidade
            return false;  // Pilacoin não validado devido a erro
        }
    }
}
