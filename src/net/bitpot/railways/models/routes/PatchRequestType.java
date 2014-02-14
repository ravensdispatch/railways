package net.bitpot.railways.models.routes;

import net.bitpot.railways.gui.RailwaysIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Basil Gren
 */
public class PatchRequestType extends RequestType {
    @Override
    public Icon getIcon() {
        return RailwaysIcons.HTTP_METHOD_PUT;
    }


    @NotNull
    @Override
    public String getName() {
        return "PATCH";
    }
}
