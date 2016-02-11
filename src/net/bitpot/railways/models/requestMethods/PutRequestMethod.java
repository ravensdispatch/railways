package net.bitpot.railways.models.requestMethods;

import net.bitpot.railways.ui.RailwaysIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Basil Gren
 */
public class PutRequestMethod extends RequestMethod {
    @Override
    public Icon getIcon() {
        return RailwaysIcons.HTTP_METHOD_PUT;
    }


    @NotNull
    @Override
    public String getName() {
        return "PUT";
    }
}
