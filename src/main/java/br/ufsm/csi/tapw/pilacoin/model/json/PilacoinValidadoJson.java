package br.ufsm.csi.tapw.pilacoin.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PublicKey;
import java.util.Date;

@Data
@Builder
@JsonDeserialize
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PilacoinValidadoJson {
    private Long id;
    private String nomeValidador;
    private byte[] chavePublicaValidador;
    private byte[] assinaturaPilaCoin;
    private PilacoinJson pilacoinJson;

    public PilacoinValidadoJson(String nomeValidador, byte[] chavePublicaValidador, byte[] assinaturaPilaCoin, PilacoinJson pilacoinJson) {
        this.nomeValidador = nomeValidador;
        this.chavePublicaValidador = chavePublicaValidador;
        this.assinaturaPilaCoin = assinaturaPilaCoin;
        this.pilacoinJson = pilacoinJson;
    }
}
