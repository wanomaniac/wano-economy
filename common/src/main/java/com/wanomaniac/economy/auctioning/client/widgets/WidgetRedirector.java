package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// Redirects events to a widget without me having to manually add the call.
public class WidgetRedirector {
    Screen liveScreen;
    List<AbstractScrollableListWidget> widgets;

    public WidgetRedirector(Screen screen){
        liveScreen = screen;
        widgets = new ArrayList<>();
    }

   public void addWidget(AbstractScrollableListWidget widget){
        widgets.add(widget);
    }

    public boolean whenKeyPressed(KeyEvent event)
    {
        List<Boolean> states = new ArrayList<>();
        for (AbstractScrollableListWidget widget : widgets){
            states.add(widget.whenKeyPressed(event));
        }

        AtomicBoolean finalState = new AtomicBoolean(false);
        states.forEach((state) -> {
            if(state) finalState.set(state);
        });

        return finalState.get();
    }

    public boolean whenMouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        List<Boolean> states = new ArrayList<>();
        for (AbstractScrollableListWidget widget : widgets){
            states.add(widget.mouseClicked(event, doubleClick));
        }

        AtomicBoolean finalState = new AtomicBoolean(false);
        states.forEach((state) -> {
            if(state) finalState.set(state);
        });

        return finalState.get();
    }


    public void whenMouseReleased(MouseButtonEvent event) {
        for (AbstractScrollableListWidget widget : widgets) {
            widget.mouseReleased(event.button());
        }
    }

    public boolean whenMouseScrolled(double mouseX, double mouseY, double scrollY) {
        List<Boolean> states = new ArrayList<>();
        for (AbstractScrollableListWidget widget : widgets){
            states.add(widget.mouseScrolled(mouseX, mouseY, scrollY));
        }

        AtomicBoolean finalState = new AtomicBoolean(false);
        states.forEach((state) -> {
            if(state) finalState.set(state);
        });

        return finalState.get();
    }

    public boolean whenMouseDragged(double y) {
        List<Boolean> states = new ArrayList<>();
        for (AbstractScrollableListWidget widget : widgets){
            states.add(widget.mouseDragged(y));
        }

        AtomicBoolean finalState = new AtomicBoolean(false);
        states.forEach((state) -> {
            if(state) finalState.set(state);
        });

        return finalState.get();
    }

    public boolean whenCharTyped(CharacterEvent event) {

        List<Boolean> states = new ArrayList<>();
        for (AbstractScrollableListWidget widget : widgets){
            states.add(widget.whenCharTyped(event));
        }

        AtomicBoolean finalState = new AtomicBoolean(false);
        states.forEach((state) -> {
            if(state) finalState.set(state);
        });

        return finalState.get();
    }
}
