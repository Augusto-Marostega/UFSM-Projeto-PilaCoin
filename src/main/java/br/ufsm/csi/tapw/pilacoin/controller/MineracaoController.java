package br.ufsm.csi.tapw.pilacoin.controller;

import br.ufsm.csi.tapw.pilacoin.service.MinerarPilacoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/mineracao")
public class MineracaoController {

    private final MinerarPilacoinService minerarPilaCoinService;

    @Autowired
    public MineracaoController(MinerarPilacoinService minerarPilaCoinService) {
        this.minerarPilaCoinService = minerarPilaCoinService;
    }

    @GetMapping("/pilacoin/iniciar")
    public String iniciarPilaCoinMineracao() {
        CompletableFuture<Void> future = minerarPilaCoinService.minerarPilacoinAsync();

        // Retorna uma mensagem indicando que a mineração foi iniciada
        return "Mineração de Pilacoin iniciada. Status: " +
                (future.isDone() ? "Concluída" : "Em andamento");
    }

    @GetMapping("/pilacoin/parar")
    public String pararPilaCoinMineracao() {
        minerarPilaCoinService.stopMining();

        // Retorna uma mensagem indicando que a mineração foi parada
        return "Mineração de Pilacoin interrompida.";
    }
}
