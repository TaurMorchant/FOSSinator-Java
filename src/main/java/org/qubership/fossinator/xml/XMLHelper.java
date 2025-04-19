package org.qubership.fossinator.xml;

import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class XMLHelper {

    public static VTDNav getVtdNav(String pomXml) throws ParseException {
        VTDGen vg = new VTDGen();
        vg.setDoc(pomXml.getBytes());
        vg.parse(true);

        return vg.getNav();
    }
}
