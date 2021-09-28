package no.hvl.tk.visual.debugger.debugging;

import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.frame.XStackFrame;
import no.hvl.tk.visual.debugger.DebugProcessListener;
import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.debugging.concurrency.CounterBasedLock;
import no.hvl.tk.visual.debugger.debugging.visualization.DebuggingInfoVisualizer;
import no.hvl.tk.visual.debugger.debugging.visualization.PlantUmlDebuggingVisualizer;
import no.hvl.tk.visual.debugger.debugging.visualization.WebSocketDebuggingVisualizer;
import no.hvl.tk.visual.debugger.settings.PluginSettingsState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DebugSessionListener implements XDebugSessionListener {
    private static final Logger LOGGER = Logger.getInstance(DebugSessionListener.class);
    private static final String CONTENT_ID = "no.hvl.tk.VisualDebugger";
    private static final String TOOLBAR_ACTION = "VisualDebugger.VisualizerToolbar"; // has to match with plugin.xml

    private final XDebugSession debugSession;
    private XStackFrame currentStackFrame;
    private JPanel userInterface;
    private DebuggingInfoVisualizer debuggingVisualizer;
    private final Set<Long> manuallyExploredObjects;

    public DebugSessionListener(final XDebugSession debugSession) {
        Objects.requireNonNull(debugSession, "Debug session must not be null.");
        this.debugSession = debugSession;
        this.manuallyExploredObjects = new HashSet<>();
    }

    @Override
    public void sessionPaused() {
        LOGGER.debug("Next step in debugger!");
        this.initUIIfNeeded();

        this.currentStackFrame = this.debugSession.getCurrentStackFrame();
        Objects.requireNonNull(this.currentStackFrame, "Stack frame unexpectedly was null.");
        this.startVisualDebugging();
    }

    public void addManuallyExploredObject(final Long objectId) {
        this.manuallyExploredObjects.add(objectId);
    }

    public void startVisualDebugging() {
        if (!SharedState.isDebuggingActive()) {
            return;
        }
        final var debuggingInfoCollector = this.getOrCreateDebuggingInfoVisualizer();
        final var lock = new CounterBasedLock();
        final var nodeVisualizer = new NodeDebugVisualizer(
                debuggingInfoCollector,
                PluginSettingsState.getInstance().getVisualisationDepth(),
                lock,
                this.manuallyExploredObjects);
        // Happens in a different thread!
        this.currentStackFrame.computeChildren(nodeVisualizer);
        new Thread(() -> {
            // Wait for the computation to be over
            lock.lock();
            debuggingInfoCollector.finishVisualization();
        }).start();
    }

    @NotNull
    public DebuggingInfoVisualizer getOrCreateDebuggingInfoVisualizer() {
        if (this.debuggingVisualizer == null) {
            switch (PluginSettingsState.getInstance().getVisualizerOption()) {
                case WEB_UI:
                    this.debuggingVisualizer = new WebSocketDebuggingVisualizer(this.userInterface);
                    break;
                case EMBEDDED:
                    this.debuggingVisualizer = new PlantUmlDebuggingVisualizer(this.userInterface);
                    break;
                default:
                    LOGGER.warn("Unrecognized debugging visualizer chosen. Defaulting to web visualizer!");
                    this.debuggingVisualizer = new WebSocketDebuggingVisualizer(this.userInterface);
            }
        }
        return this.debuggingVisualizer;
    }

    private void initUIIfNeeded() {
        if (this.userInterface != null) {
            return;
        }
        this.userInterface = new JPanel();
        this.getOrCreateDebuggingInfoVisualizer(); // make sure visualizer is initialized
        if (!SharedState.isDebuggingActive()) {
            this.resetUIAndAddActivateDebuggingButton();
        } else {
            this.debuggingVisualizer.debuggingActivated();
        }
        final var uiContainer = new SimpleToolWindowPanel(false, true);

        final var actionManager = ActionManager.getInstance();
        final var actionToolbar = actionManager.createActionToolbar(
                TOOLBAR_ACTION,
                (DefaultActionGroup) actionManager.getAction(TOOLBAR_ACTION),
                false
        );
        actionToolbar.setTargetComponent(this.userInterface);
        uiContainer.setToolbar(actionToolbar.getComponent());
        uiContainer.setContent(this.userInterface);

        final RunnerLayoutUi ui = this.debugSession.getUI();
        final var content = ui.createContent(
                CONTENT_ID,
                uiContainer,
                "Visual Debugger",
                IconLoader.getIcon("/icons/icon_16x16.png", DebugProcessListener.class),
                null);
        content.setCloseable(false);
        UIUtil.invokeLaterIfNeeded(() -> ui.addContent(content));
        LOGGER.debug("UI initialized!");
    }

    public void resetUIAndAddActivateDebuggingButton() {
        this.userInterface.removeAll();
        this.userInterface.setLayout(new FlowLayout());

        final var activateButton = new JButton("Activate visual debugger");
        activateButton.addActionListener(actionEvent -> {

            SharedState.setDebuggingActive(true);
            this.userInterface.remove(activateButton);
            this.debuggingVisualizer.debuggingActivated();
            this.userInterface.revalidate();
            this.startVisualDebugging();
        });
        this.userInterface.add(activateButton);

        this.userInterface.revalidate();
        this.userInterface.repaint();
    }
}
