package org.arcanum.pairing.g;

import org.arcanum.Element;
import org.arcanum.Point;
import org.arcanum.Polynomial;
import org.arcanum.Vector;
import org.arcanum.field.curve.CurveField;
import org.arcanum.field.gt.GTFiniteElement;
import org.arcanum.field.gt.GTFiniteField;
import org.arcanum.field.poly.PolyModElement;
import org.arcanum.pairing.map.AbstractMillerPairingMap;

public class TypeGTateAffineNoDenomMillerPairingMap extends AbstractMillerPairingMap<Polynomial> {
    protected TypeGPairing pairing;


    public TypeGTateAffineNoDenomMillerPairingMap(TypeGPairing pairing) {
        super(pairing);

        this.pairing = pairing;
    }


    public Element pairing(Point in1, Point in2) {
        // map from twist: (x, y) --> (v^-1 x, v^-(3/2) y)
        // where v is the quadratic non-residue used to construct the twist
        Polynomial Qx = (Polynomial) in2.getX().duplicate().mul(pairing.nqrInverse);
        // v^-3/2 = v^-2 * v^1/2
        Polynomial Qy = (Polynomial) in2.getY().duplicate().mul(pairing.nqrInverseSquare);

        return new GTFiniteElement(this, (GTFiniteField) pairing.getGT(), tatePow(pairing(in1, Qx, Qy)));
    }

    public void finalPow(Element element) {
        element.set(tatePow(element));
    }

    public boolean isAlmostCoddh(Element a, Element b, Element c, Element d) {
        // Twist: (x, y) --> (v^-1 x, v^-(3/2) y)
        // where v is the quadratic nonresidue used to construct the twist.

        Element cx = ((Point) c).getX().duplicate().mul(pairing.nqrInverse);
        Element dx = ((Point) d).getX().duplicate().mul(pairing.nqrInverse);

        // v^-3/2 = v^-2 * v^1/2
        Element cy = ((Point) c).getY().duplicate().mul(pairing.nqrInverseSquare);
        Element dy = ((Point) d).getY().duplicate().mul(pairing.nqrInverseSquare);

        Element t0 = pairing((Point) a, (Polynomial) dx, (Polynomial) dy);
        Element t1 = pairing((Point) b, (Polynomial) cx, (Polynomial) cy);
        t0 = tatePow(t0);
        t1 = tatePow(t1);
        Element t2 = t0.duplicate().mul(t1);

        if (t2.isOne())
            return true;    // We were given g, g^x, h, h^-x.
        else {
          // Cheaply check the other case.
            t2.set(t0).mul(t1.invert());
            if (t2.isOne())
                return true;    // We were given g, g^x, h, h^x.
        }

        return false;
    }


    public Element tatePow(Element element) {
        Point<Polynomial> e0, e3;
        PolyModElement e2;

        e0 = pairing.Fqk.newElement();
        e2 = pairing.Fqd.newElement();
        e3 = pairing.Fqk.newElement();

        Polynomial e0re = e0.getX();
        Polynomial e0im = e0.getY();

        Element e0re0 = e0re.getAt(0);
        Element e0im0 = e0im.getAt(0);

        Point<Polynomial> in = (Point<Polynomial>) element;
        Vector<Element> inre = in.getX();
        Vector<Element> inmi = in.getY();

        qPower(1, e2, e0re, e0im, e0re0, e0im0, inre, inmi);
        e3.set(e0);
        e0re.set(in.getX());
        e0im.set(in.getY()).negate();
        e3.mul(e0);
        qPower(-1, e2, e0re, e0im, e0re0, e0im0, inre, inmi);
        e0.mul(in);
        e0.invert();
        in.set(e3).mul(e0);
        e0.set(in);

        return lucasEven(e0, pairing.phikOnr);
    }

    final void qPower(int sign, PolyModElement e2,
                      Element e0re, Element e0im, Element e0re0, Element e0im0,
                      Vector<Element> inre, Vector<Element> inim) {
        e2.set(pairing.xPowq).polymodConstMul(inre.getAt(1));
        e0re.set(e2);
        e2.set(pairing.xPowq2).polymodConstMul(inre.getAt(2));
        e0re.add(e2);
        e2.set(pairing.xPowq3).polymodConstMul(inre.getAt(3));
        e0re.add(e2);
        e2.set(pairing.xPowq4).polymodConstMul(inre.getAt(4));
        e0re.add(e2);
        e0re0.add(inre.getAt(0));

        if (sign > 0) {
            e2.set(pairing.xPowq).polymodConstMul(inim.getAt(1));
            e0im.set(e2);
            e2.set(pairing.xPowq2).polymodConstMul(inim.getAt(2));
            e0im.add(e2);
            e2.set(pairing.xPowq3).polymodConstMul(inim.getAt(3));
            e0im.add(e2);
            e2.set(pairing.xPowq4).polymodConstMul(inim.getAt(4));
            e0im.add(e2);

            e0im0.add(inim.getAt(0));
        } else {
            e2.set(pairing.xPowq).polymodConstMul(inim.getAt(1));
            e0im.set(e2).negate();
            e2.set(pairing.xPowq2).polymodConstMul(inim.getAt(2));
            e0im.sub(e2);
            e2.set(pairing.xPowq3).polymodConstMul(inim.getAt(3));
            e0im.sub(e2);
            e2.set(pairing.xPowq4).polymodConstMul(inim.getAt(4));
            e0im.sub(e2);
            e0im0.sub(inim.getAt(0));
        }
    }


    protected Element pairing(Point P, Polynomial Qx, Polynomial Qy) {
        Element Px = P.getX();
        Element Py = P.getY();

        Point Z = (Point) P.duplicate();
        Element Zx = Z.getX();
        Element Zy = Z.getY();

        Element a = Px.getField().newElement();
        Element b = a.duplicate();
        Element c = a.duplicate();
        Element cca = ((CurveField) P.getField()).getA();
        Element temp = a.duplicate();

        Point<Polynomial> f0 = pairing.Fqk.newElement();
        Element f = pairing.Fqk.newOneElement();

        for (int m = pairing.r.bitLength() - 2; m > 0; m--) {
            tangentStep(f0, a, b, c, Zx, Zy, cca, temp, Qx, Qy, f);
            Z.twice();

            if (pairing.r.testBit(m)) {
                lineStep(f0, a, b, c, Zx, Zy, Px, Py, temp, Qx, Qy, f);
                Z.add(P);
            }

            f.square();
        }
        tangentStep(f0, a, b, c, Zx, Zy, cca, temp, Qx, Qy, f);

        return f;
    }

    protected void millerStep(Point<Polynomial> out, Element a, Element b, Element c, Polynomial Qx, Polynomial Qy) {
        // a, b, c are in Fq
        // point Q is (Qx, Qy * sqrt(nqr)) where nqr is used to construct
        // the quadratic field extension Fqk of Fqd

        Polynomial rePart = out.getX();
        Polynomial imPart = out.getY();

        int i;
        //int d = rePart.getField().getN();
        int d = rePart.getDegree();
        for (i = 0; i < d; i++) {
            rePart.getAt(i).set(Qx.getAt(i)).mul(a);
            imPart.getAt(i).set(Qy.getAt(i)).mul(b);
        }

        rePart.getAt(0).add(c);
    }

}