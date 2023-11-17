package br.ufsm.csi.tapw.pilacoin.model.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@JsonDeserialize
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PilacoinJson {
    @JsonIgnore
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date dataCriacao;
    private byte[] chaveCriador;
    private String nomeCriador;
    private String nonce;
    private String status;


    public PilacoinJson(Date dataCriacao, byte[] chaveCriador, String nomeCriador, String nonce) {
        this.dataCriacao = dataCriacao;
        this.chaveCriador = chaveCriador;
        this.nomeCriador = nomeCriador;
        this.nonce = nonce;
    }

    @Override
    public PilacoinJson clone(){
        try {
            PilacoinJson clone = (PilacoinJson) super.clone();
            return clone;
        } catch (CloneNotSupportedException e){
            throw new AssertionError();
        }

    }
}
