package pt.utl.ist.thesis.signalprocessor;

import pt.utl.ist.util.filters.MovingAverageFilter;

public interface FilterVisitor {

	 void visit(MovingAverageFilter avg);
	 
	 
}
