package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Dificuldade;
import br.ufsm.csi.tapw.pilacoin.model.json.BlocoJson;
import br.ufsm.csi.tapw.pilacoin.model.json.BlocoValidadoJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinValidadoJson;
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
public class ValidarBlocoService {

    private static final Logger logger = LoggerFactory.getLogger(ValidarBlocoService.class);

    private final RSAKeyPairGenerator rsaKeyPairGenerator;
    private final DificuldadeService dificuldadeService;
    private final RabbitMQService rabbitMQService;
    private final AtomicBoolean miningStopped = new AtomicBoolean(false);
    private final PilacoinDataHandler pilacoinDataHandler;

    @Autowired
    public ValidarBlocoService(
            PilacoinDataHandler pilacoinDataHandler,
            RSAKeyPairGenerator rsaKeyPairGenerator,
            DificuldadeService dificuldadeService,
            RabbitMQService rabbitMQService) {
        this.pilacoinDataHandler = pilacoinDataHandler;
        this.rsaKeyPairGenerator = rsaKeyPairGenerator;
        this.dificuldadeService = dificuldadeService;
        this.rabbitMQService = rabbitMQService;
    }

    public CompletableFuture<Void> validarBlocoAsync(String strBlocoJson) {
        try {
            return CompletableFuture.runAsync(() -> {
                logger.info("[validarBlocoAsync] Iniciando Validação de bloco.");
                if(!validarBloco(strBlocoJson)){
                    //erro ao validar, reenviar "strBlocoJson" para a fila bloco-validado
                    logger.error("[validarBlocoAsync] Bloco NÃO Validado, reenviando para a fila 'bloco-validado'.");
                    rabbitMQService.enviarMensagemParaFila("bloco-validado", strBlocoJson);
                }
            });
        } catch (Exception e) {
            logger.error("[validarBlocoAsync] Erro ao iniciar validação de bloco assíncrona.", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public void stopMining() {
        miningStopped.set(true);
    }

    private boolean validarBloco(String strBlocoJson) {
        try {
            logger.info("[validarBloco] Iniciando validação do Bloco: {}", strBlocoJson);
            BlocoJson blocoJson = pilacoinDataHandler.strParaObjBlocoJson(strBlocoJson);

            if (blocoJson.getNonce() == null || blocoJson.getNomeUsuarioMinerador() == null || blocoJson.getChaveUsuarioMinerador() == null) {
                logger.error("[validarBloco] Bloco com atributos obrigatórios null. Ignorando validação. Retornando false para reenviar para a fila 'pila-minerado'.");
                return false;
            } else if ("Augusto".equals(blocoJson.getNomeUsuarioMinerador())) {
                // Se o criador é "Augusto", reenvia para a fila "pila-minerado"
                logger.warn("[validarBloco] Bloco criado por Augusto. Ignorando validação. Retornando false para reenviar para a fila 'pila-minerado'.");
                return false;
            } else {
                // Se não for "Augusto", o Pilacoin deve ser validado
                byte[] hash = pilacoinDataHandler.getHash(strBlocoJson); // gerando HASH da string original
                BigInteger hashBigInt = new BigInteger(hash).abs(); // Converter hash para BigInteger para comparar com a dificuldade
                Dificuldade ultimaDificuldade = dificuldadeService.getUltimaDificuldade();
                if (ultimaDificuldade == null || ultimaDificuldade.getValidadeFinal() == null){
                    logger.error("[validarBloco] Não pode validar Bloco -- ultimaDificuldade é null ou sem validade final..");
                    return false;
                }
                if (ultimaDificuldade.getValidadeFinal().compareTo(new Date()) > 0){
                    logger.error("[validarBloco] Não pode validar Bloco -- ultimaDificuldade está vencida.");
                    return false;
                }
                if (hashBigInt.compareTo(ultimaDificuldade.getDificuldade()) < 0) {
                    // Bloco validado com sucesso -- assinar bloco
                    logger.info("[processarPilacoin] Bloco validado, gerando assinatura.");
                    byte[] assinaturaBloco = pilacoinDataHandler.gerarAssinatura(blocoJson, rsaKeyPairGenerator.generateOrLoadKeyPair().getPrivate()); //assinatura do obj blocoJson
                    BlocoValidadoJson blocoValidadoJson = new BlocoValidadoJson("Augusto", rsaKeyPairGenerator.generateOrLoadKeyPair().getPublic().getEncoded(), assinaturaBloco, blocoJson);
                    //logger.info("[validarBloco] Bloco assinado JSON: {}", pilacoinDataHandler.blocoValidadoJsonParaStrJson(blocoValidadoJson));
                    rabbitMQService.enviarMensagemParaFila("bloco-validado", pilacoinDataHandler.blocoValidadoJsonParaStrJson(blocoValidadoJson));
                    return true;
                }
            }
            logger.warn("[validarBloco] HASH não é menor que dificuldade...");
            return false;
        } catch (Exception e) {
            logger.error("[processarPilacoin] Erro ao processar Pilacoin: {}", e.getMessage());
            // Em caso de erro, trata de alguma forma (log, lançar exceção, etc.)
            // Pode ser necessário ajustar conforme sua necessidade
            return false;  // Pilacoin não validado devido a erro
        }
    }
}