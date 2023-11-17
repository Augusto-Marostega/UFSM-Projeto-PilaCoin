package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Dificuldade;
import br.ufsm.csi.tapw.pilacoin.model.json.BlocoJson;
import br.ufsm.csi.tapw.pilacoin.model.json.BlocoValidadoJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinJson;
import br.ufsm.csi.tapw.pilacoin.util.PilacoinDataHandler;
import br.ufsm.csi.tapw.pilacoin.util.RSAKeyPairGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MinerarBlocoService {

    private static final Logger logger = LoggerFactory.getLogger(MinerarBlocoService.class);

    private final RSAKeyPairGenerator rsaKeyPairGenerator;
    private final DificuldadeService dificuldadeService;
    private final RabbitMQService rabbitMQService;
    private final AtomicBoolean miningStopped = new AtomicBoolean(false);
    private final PilacoinDataHandler pilacoinDataHandler;

    @Autowired
    public MinerarBlocoService(
            PilacoinDataHandler pilacoinDataHandler,
            RSAKeyPairGenerator rsaKeyPairGenerator,
            DificuldadeService dificuldadeService,
            RabbitMQService rabbitMQService) {
        this.pilacoinDataHandler = pilacoinDataHandler;
        this.rsaKeyPairGenerator = rsaKeyPairGenerator;
        this.dificuldadeService = dificuldadeService;
        this.rabbitMQService = rabbitMQService;
    }

    public CompletableFuture<Void> minerarBlocoAsync(String strBlocoJson) {
        try {
            return CompletableFuture.runAsync(() -> {
                logger.info("[minerarBlocoAsync] Iniciando mineração de bloco.");
                minerarBloco(strBlocoJson);
            });
        } catch (Exception e) {
            logger.error("[minerarBlocoAsync] Erro ao iniciar mineração assíncrona.", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public void stopMining() {
        miningStopped.set(true);
    }

    private void minerarBloco(String strBlocoJson) {
        try {
            KeyPair keyPair = rsaKeyPairGenerator.generateOrLoadKeyPair();
            Dificuldade ultimaDificuldade = dificuldadeService.getUltimaDificuldade();

            Random rnd = new SecureRandom();
            PublicKey chaveCriador = keyPair.getPublic();
            String nomeCriador = "Augusto";

            if (ultimaDificuldade == null || ultimaDificuldade.getValidadeFinal() == null){
                logger.error("[minerarBloco] ultimaDificuldade é null.");
                stopMining();
            }
            if (ultimaDificuldade.getValidadeFinal().compareTo(new Date()) > 0){
                logger.error("[minerarBloco] ultimaDificuldade está vencida.");
                stopMining();
            }
            BlocoJson blocoJson = pilacoinDataHandler.strParaObjBlocoJson(strBlocoJson);
            blocoJson.setChaveUsuarioMinerador(chaveCriador.getEncoded());
            blocoJson.setNomeUsuarioMinerador(nomeCriador);
            while (!miningStopped.get()) {
                String nonce = String.valueOf(new BigInteger(256, rnd).abs());
                blocoJson.setNonce(nonce);

                String blocoJsonString = pilacoinDataHandler.blocoJsonParaStrJson(blocoJson); //convertendo objeto em JSON
                byte[] hash = pilacoinDataHandler.getHash(blocoJsonString); //gerando HASH da String JSON
                //Converter hash para um BigInteger
                BigInteger hashBigInt = new BigInteger(hash).abs();

                if (hashBigInt.compareTo(ultimaDificuldade.getDificuldade()) < 0) {
                    // Bloco foi minerado com sucesso
                    logger.info("[minerarBloco] Bloco minerado com sucesso: {}", blocoJsonString);
                    enviarBlocoParaFila(blocoJsonString);
                    break;  // Saia do loop se o bloco for minerado com sucesso
                }
            }

            logger.warn("[minerarBloco] Saindo do processo de mineração de bloco.");

        } catch (Exception e) {
            logger.error("[minerarBloco] Erro durante a mineração de bloco.", e);
        }
    }

    private void enviarBlocoParaFila(String pilaBlocoJsonString) {
        rabbitMQService.enviarMensagemParaFila("bloco-minerado", pilaBlocoJsonString);
        logger.info("[enviarBlocoParaFila] Bloco minerado, enviado para a fila 'bloco-minerado'.");
    }

}
