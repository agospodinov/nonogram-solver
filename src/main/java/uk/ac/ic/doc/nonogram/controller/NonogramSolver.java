package uk.ac.ic.doc.nonogram.controller;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import uk.ac.ic.doc.nonogram.model.Nonogram;

public class NonogramSolver {

    public static final String SOLVER_CONFIG = "uk/ac/ic/doc/nonogram/nonogramSolverConfig.xml";

    private Nonogram nonogram;
    private Solver<Nonogram> solver;
    private Thread workerThread;

    public NonogramSolver() {
        SolverFactory<Nonogram> solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG);
        solver = solverFactory.buildSolver();
    }

    public synchronized void initialize(Integer width, Integer height) {
        if (solver.isSolving()) {
            throw new IllegalStateException("Cannot create a new nonogram while solver is running");
        }

        nonogram = new Nonogram(width, height);
    }

    public synchronized void solve(SolverEventListener<Nonogram> eventListener) {
        solver.addEventListener(eventListener);

        workerThread = new Thread(() -> {
            solver.solve(nonogram);
//            solver.addEventListener(this::onBestSolutionUpdated);
        });

        workerThread.start();
    }

    public synchronized Nonogram getNonogram() {
        return nonogram;
    }

    private synchronized void onBestSolutionUpdated(BestSolutionChangedEvent<Nonogram> event) {
        nonogram = event.getNewBestSolution();
    }

    public boolean isSolving() {
        return solver.isSolving();
    }

    public void terminate() {
        solver.terminateEarly();
    }
}
