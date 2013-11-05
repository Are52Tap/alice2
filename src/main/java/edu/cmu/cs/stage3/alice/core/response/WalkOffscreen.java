/*
 * Created on Feb 9, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.cmu.cs.stage3.alice.core.response;

import edu.cmu.cs.stage3.alice.core.Property;
import edu.cmu.cs.stage3.alice.core.camera.SymmetricPerspectiveCamera;
import edu.cmu.cs.stage3.math.Matrix33;

/**
 * @author caitlin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WalkOffscreen extends AbstractWalkAnimation {
	public final edu.cmu.cs.stage3.alice.core.property.DirectionProperty exitDirection = new edu.cmu.cs.stage3.alice.core.property.DirectionProperty(this, "exit direction", edu.cmu.cs.stage3.alice.core.Direction.RIGHT);
	
	double turnLength = 0.25;
	
	
	protected void propertyChanged( Property property, Object value ) {
		super.propertyChanged(property, value);
		if (property.equals(duration)) {
			if (Double.isNaN( ((Double)value).doubleValue()) ){
			} else {
				if ( duration.doubleValue() > 2) {
					turnLength = 0.5;
				} else {
					turnLength = duration.doubleValue()/5;
				}
			}
		} else if (property.equals(stepSpeed)){
			if (Double.isNaN( ((Double)value).doubleValue()) ){
			} else {
				turnLength = 0.5;
			}
		}		
	}
	
	public class RuntimeWalkOffScreen extends RuntimeAbstractWalkAnimation implements edu.cmu.cs.stage3.alice.scenegraph.event.AbsoluteTransformationListener {
				
		private edu.cmu.cs.stage3.math.Matrix33 m_orient0;
		private edu.cmu.cs.stage3.math.Matrix33 m_orient1;
		
		private boolean firstOver1 = true;
		
		protected edu.cmu.cs.stage3.alice.core.Camera camera = null;
		protected double cameraAngle = 0.0;
		protected double distanceToCenter = 0.0;
		
		protected double distanceToMove = 0.0;
		double timePerStep = -1.0;
		
		double stepLength = -1.0;	
		double numberOfSteps = -1.0;
		double currentPos = 0.0;
		
		double boundingBoxDepth = -1.0;
		
		//double turnLength = 0.25;
		
		public void absoluteTransformationChanged( edu.cmu.cs.stage3.alice.scenegraph.event.AbsoluteTransformationEvent absoluteTransformationEvent ) {
			//findCamera();
			//stepLength = -1.0;
			//numberOfSteps = -1.0;
			//getActualStepLength();
		}

		
		public void prologue( double t) {
			super.prologue(t);
			findCamera();
			getActualStepLength();
			
			m_orient0 = subject.getOrientationAsAxes(camera);
			m_orient1 = new Matrix33();
			
			if (exitDirection.getDirectionValue().equals(edu.cmu.cs.stage3.alice.core.Direction.LEFT)) {
				m_orient1.setForwardUpGuide(new javax.vecmath.Vector3d(-1,0,0), m_orient0.getRow(1));
			} else {
				m_orient1.setForwardUpGuide(new javax.vecmath.Vector3d(1,0,0), m_orient0.getRow(1));
			}
					
		}
		
		protected double getActualStepLength() {
			if (stepLength == -1) {
				stepLength = this.getStepLength();
				if (stepLength == 0.0) stepLength = 1.0;
			}
	
			if (numberOfSteps == -1) {
				numberOfSteps = java.lang.Math.round(distanceToMove/stepLength);
			}
	
			return distanceToMove/(numberOfSteps);
	
		}
		
		
		public double getTimeRemaining( double t ) {
			double walkTime = duration.doubleValue() - turnLength;
			double totalTime = walkTime + turnLength;
			
			if (Double.isNaN(walkTime)) {
				walkTime = numberOfSteps/WalkOffscreen.this.stepSpeed.doubleValue();
				totalTime = walkTime + turnLength;
			}
			
			return totalTime - getTimeElapsed(t);
		}
		
		
		protected double getPortion( double t ) {
			double duration = getDuration();
			if (Double.isNaN(duration)) {
				duration = numberOfSteps/WalkOffscreen.this.stepSpeed.doubleValue() + turnLength;
			}
			return m_style.getPortion( Math.min( getTimeElapsed( t ), duration ), duration );
		}
		
		
		public void update( double t ) {	
						
			if (getTimeElapsed(t) <= turnLength) {	
				double portion = m_style.getPortion( Math.min( getTimeElapsed( t ), turnLength ), turnLength );
				
				edu.cmu.cs.stage3.math.Matrix33 q = edu.cmu.cs.stage3.math.Matrix33.interpolate( m_orient0, m_orient1, portion );
				subject.setOrientationRigh