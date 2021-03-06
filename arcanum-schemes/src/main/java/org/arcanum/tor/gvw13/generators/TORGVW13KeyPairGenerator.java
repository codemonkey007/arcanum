package org.arcanum.tor.gvw13.generators;


import org.arcanum.Field;
import org.arcanum.common.cipher.engine.ElementCipher;
import org.arcanum.common.cipher.generators.ElementKeyPairGenerator;
import org.arcanum.common.cipher.params.ElementKeyPairParameters;
import org.arcanum.tor.gvw13.params.TORGVW13KeyPairGenerationParameters;
import org.arcanum.tor.gvw13.params.TORGVW13PublicKeyParameters;
import org.arcanum.tor.gvw13.params.TORGVW13SecretKeyParameters;
import org.arcanum.trapdoor.mp12.engines.MP12ErrorTolerantOneTimePad;
import org.arcanum.trapdoor.mp12.engines.MP12HLP2OneWayFunction;
import org.arcanum.trapdoor.mp12.generators.MP12HLP2KeyPairGenerator;
import org.arcanum.trapdoor.mp12.params.MP12HLP2KeyPairGenerationParameters;
import org.arcanum.trapdoor.mp12.params.MP12HLP2OneWayFunctionParameters;
import org.arcanum.trapdoor.mp12.params.MP12HLP2PublicKeyParameters;

/**
 * @author Angelo De Caro (arcanumlib@gmail.com)
 */
public class TORGVW13KeyPairGenerator implements ElementKeyPairGenerator<TORGVW13KeyPairGenerationParameters> {

    private TORGVW13KeyPairGenerationParameters params;
    private MP12HLP2KeyPairGenerator gen;


    public TORGVW13KeyPairGenerator init(TORGVW13KeyPairGenerationParameters keyGenerationParameters) {
        this.params = keyGenerationParameters;

        // Init Lattice generator
        // TODO: k must be chosen differently
        gen = new MP12HLP2KeyPairGenerator();
        gen.init(new MP12HLP2KeyPairGenerationParameters(
                params.getRandom(),
                params.getParameters().getN(),  // n
                64                              // k
        ));

        return this;
    }

    public ElementKeyPairParameters generateKeyPair() {
        ElementKeyPairParameters keyPair = gen.generateKeyPair();

        // One-way function
        ElementCipher owf = new MP12HLP2OneWayFunction();
        MP12HLP2OneWayFunctionParameters owfParams = new MP12HLP2OneWayFunctionParameters(
                (MP12HLP2PublicKeyParameters) keyPair.getPublic()
        );
        owf.init(owfParams);

        // error-tolerant version of the one-time pad
        ElementCipher otp = new MP12ErrorTolerantOneTimePad();

        return new ElementKeyPairParameters(
                new TORGVW13PublicKeyParameters(
                        params.getParameters(),
                        keyPair.getPublic(),
                        owf,
                        gen.getInputField(),
                        gen.getOutputField(),
                        otp
                ),
                new TORGVW13SecretKeyParameters(
                        params.getParameters(),
                        keyPair.getPrivate(),
                        gen.getOutputField()
                )
        );
    }


    public int getKeyLengthInBytes() {
        return gen.getMInBytes();
    }

    public Field getOwfInputField() {
        return gen.getInputField();
    }
}
