package br.ufsm.csi.tapw.pilacoin.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
@JsonDeserialize
public class Dificuldade {
    private BigInteger dificuldade;
    private Date inicio;
    private Date validadeFinal;

    public Dificuldade(BigInteger dificuldade, Date inicio, Date validadeFinal) {
        this.dificuldade = dificuldade;
        this.inicio = inicio;
        this.validadeFinal = validadeFinal;
    }
    public Dificuldade() {
    }
}
