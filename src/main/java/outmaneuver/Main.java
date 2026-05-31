package outmaneuver;

import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneImpl;
import outmaneuver.model.area.StandardStats;
import outmaneuver.view.swing.SwingGameView;

public final class Main {

    private Main() {
    }

    public static void main(final String[] args) {
        final Plane plane = new PlaneImpl(new StandardStats());
        final InputControllerImpl inputCtrl = new InputControllerImpl();

        final MasterControllerImpl[] masterRef = new MasterControllerImpl[1];
        final EntityControllerImpl entityCtrl = new EntityControllerImpl(
                plane, inputCtrl,
                (evt, data) -> masterRef[0].onInternalEvent(evt, data));
        final MasterControllerImpl master = new MasterControllerImpl(entityCtrl);
        masterRef[0] = master;

        final SwingGameView view = new SwingGameView(inputCtrl, master);
        view.init();
        master.attachView(view);
        master.start();
    }
}
