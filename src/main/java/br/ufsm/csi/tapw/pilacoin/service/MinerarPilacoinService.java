package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Dificuldade;
import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinJson;
import br.ufsm.csi.tapw.pilacoin.util.PilacoinDataHandler;
import br.ufsm.csi.tapw.pilacoin.util.RSAKeyPairGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
public class MinerarPilacoinService {

    private static final Logger logger = LoggerFactory.getLogger(MinerarPilacoinService.class);

    private final RSAKeyPairGenerator rsaKeyPairGenerator;
    private final DificuldadeService dificuldadeService;
    private final RabbitMQService rabbitMQService;
    private final AtomicBoolean miningStopped = new AtomicBoolean(false);
    private final PilacoinDataHandler pilacoinDataHandler;

    @Autowired
    public MinerarPilacoinService(
            PilacoinDataHandler pilacoinDataHandler,
            RSAKeyPairGenerator rsaKeyPairGenerator,
            DificuldadeService dificuldadeService,
            RabbitMQService rabbitMQService) {
        this.pilacoinDataHandler = pilacoinDataHandler;
        this.rsaKeyPairGenerator = rsaKeyPairGenerator;
        this.dificuldadeService = dificuldadeService;
        this.rabbitMQService = rabbitMQService;
    }

    @Async
    public CompletableFuture<Void> minerarPilacoinAsync() {
        try {
            return CompletableFuture.runAsync(() -> {
                logger.info("[minerarPilacoinAsync] Iniciando minerarPilaCoin();");
                minerarPilacoin();
            });
        } catch (Exception e) {
            logger.error("[minerarPilacoinAsync] Erro ao iniciar mineração assíncrona.", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public void stopMining() {
        miningStopped.set(true);
    }

    private void minerarPilacoin() {
        try {
            KeyPair keyPair = rsaKeyPairGenerator.generateOrLoadKeyPair();
            Dificuldade ultimaDificuldade = dificuldadeService.getUltimaDificuldade();

            Random rnd = new SecureRandom();
            PublicKey chaveCriador = keyPair.getPublic();
            String nomeCriador = "Augusto";

            if (ultimaDificuldade == null || ultimaDificuldade.getValidadeFinal() == null){
                logger.error("[minerarPilacoin] ultimaDificuldade é null.");
                stopMining();
            }
            if (ultimaDificuldade.getValidadeFinal().compareTo(new Date()) < 0){
                logger.error("[minerarPilacoin] ultimaDificuldade esta vencida.");
                stopMining();
            }

            while (!miningStopped.get()) {
                String nonce = String.valueOf(new BigInteger(256, rnd));

                PilacoinJson pilaCoinJson = PilacoinJson.builder()
                        .dataCriacao(new Date())
                        .chaveCriador(chaveCriador.getEncoded())
                        .nomeCriador(nomeCriador)
                        .nonce(nonce)
                        .build();

                // Converter PilacoinJson para String JSON
                String pilacoinJsonString = pilacoinDataHandler.pilacoinJsonParaStrJson(pilaCoinJson);

                // Gerar hash SHA-256 da String JSON
                byte[] hash = pilacoinDataHandler.getHash(pilaCoinJson); //**USANDO HASH DO OBJETO PilacoinJson**
                //Converter hash para um BigInteger
                BigInteger hashBigInt = new BigInteger(hash).abs();
                if (hashBigInt.compareTo(ultimaDificuldade.getDificuldade()) < 0) {
                    // Pilacoin minerado com sucesso
                    logger.info("[minerarPilacoin] Pilacoin minerado: {}", pilacoinJsonString);
                    System.out.println(pilacoinJsonString);
                    enviarPilaCoinParaFila(pilacoinJsonString);

                    break;  // Saia do loop se o Pilacoin for minerado com sucesso
                }
            }
            logger.warn("[minerarPilacoin] saiu do while da mineração.");

        } catch (Exception e) {
            logger.error("[minerarPilacoin] Erro durante a mineração do Pilacoin.", e);
        }
    }

    private void enviarPilaCoinParaFila(String pilaCoinJsonString) {
        rabbitMQService.enviarMensagemParaFila("pila-minerado", pilaCoinJsonString);
        logger.info("[enviarPilaCoinParaFila] Pilacoin minerado enviado para a fila 'pila-minerado'.");
    }
}
