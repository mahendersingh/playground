package abe;

import java.io.IOException;
import java.util.Collection;

import jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.reporting.SolutionPrinter.Print;
import jsprit.core.util.Solutions;
import jsprit.core.util.VehicleRoutingTransportCostsMatrix;

public class AbeProblemRandomSpeedup {
	
	
	public static void main(String[] args) throws IOException {
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpBuilder).read("/Users/schroeder/Documents/jsprit/abraham/abrahamProblem.xml");
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		final MatrixReader matrixReader = new MatrixReader(matrixBuilder,0.5,0.5);
		matrixReader.read("/Users/schroeder/Documents/jsprit/abraham/Matrix.txt");
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		
		vrpBuilder.setRoutingCost(matrix);
		VehicleRoutingProblem problem = vrpBuilder.build();
		
		Plotter plotter = new Plotter(problem);
		plotter.plot("output/abeProblem.png", "abe");
		
//		VehicleRoutingAlgorithm algo = new SchrimpfFactory().createAlgorithm(problem); 
		VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.readAndCreateAlgorithm(problem, "/Users/schroeder/Documents/jsprit/abraham/algorithmConfig_stefan.xml");
		algo.addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
//		VariationCoefficientTermination prematureAlgorithmTermination = new VariationCoefficientTermination(200, 0.01);
//		algo.addListener(prematureAlgorithmTermination);
//		algo.setPrematureAlgorithmTermination(prematureAlgorithmTermination);
		Collection<VehicleRoutingProblemSolution> solutions = algo.searchSolutions();
		
		Plotter plotter2 = new Plotter(problem,Solutions.bestOf(solutions));
		plotter2.setShowFirstActivity(true);
		plotter2.plot("output/abeProblemWithSolution.png", "abe");
		
		SolutionPrinter.print(problem, Solutions.bestOf(solutions), Print.VERBOSE);
		
		System.out.println("total-time: " + getTotalTime(problem, Solutions.bestOf(solutions)));
		System.out.println("total-distance: " + getTotalDistance(matrixReader, Solutions.bestOf(solutions)));
		
	}

	private static double getTotalDistance(MatrixReader matrix,VehicleRoutingProblemSolution bestOf) {
		double dist = 0.0;
		for(VehicleRoute r : bestOf.getRoutes()){
			TourActivity last = r.getStart();
			for(TourActivity act : r.getActivities()){
				dist += matrix.getDistance(last.getLocationId(), act.getLocationId());
				last=act;
			}
			dist+=matrix.getDistance(last.getLocationId(), r.getEnd().getLocationId());
		}
		return dist;
	}

	private static double getTotalTime(VehicleRoutingProblem problem,VehicleRoutingProblemSolution bestOf) {
		double time = 0.0;
		for(VehicleRoute r : bestOf.getRoutes()) time+=r.getEnd().getArrTime();
		return time;
	}

}
