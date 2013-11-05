package edu.cmu.cs.stage3.alice.gallery.batch;

import edu.cmu.cs.stage3.alice.authoringtool.JAlice;

public class BatchSaveWithThumbnails extends Batch {
	public edu.cmu.cs.stage3.alice.core.camera.SymmetricPerspectiveCamera m_camera;
	public edu.cmu.cs.stage3.alice.scenegraph.renderer.OffscreenRenderTarget m_rt;
	
	protected void initialize( edu.cmu.cs.stage3.alice.core.World world ) {
		final edu.cmu.cs.stage3.alice.authoringtool.util.Configuration authoringtoolConfig = edu.cmu.cs.stage3.alice.authoringtool.util.Configuration.getLocalConfiguration( JAlice.class.getPackage() );
		m_camera = new edu.cmu.cs.stage3.alice.core.camera.SymmetricPerspectiveCamera();
		m_camera.vehicle.set( world );
		m_camera.nearClippingPlaneDistance.set( new Double( .1 ) );
		m_camera.verticalViewingAngle.set( new Double( Math.PI/8.0 ) );
		m_camera.horizontalViewingAngle.set( new Double( Math.PI/8.0 ) );
		world.addChild( m_camera );

		final double brightness = 128/255.0;
		world.atmosphereColor.set( new edu.cmu.cs.stage3.alice.scenegraph.Color( brightness, brightness, brightness ) );
		Class rendererClass = null;
		try {
			String[] renderers = authoringtoolConfig.getValueList( "rendering.orderedRendererList" ); 
			rendererClass = Class.forName( renderers[ 0 ] );
		} catch( Throwable t ) { }
		
		edu.cmu.cs.stage3.alice.scenegraph.renderer.DefaultRenderTargetFactory rtf = new edu.cmu.cs.stage3.alice.scenegraph.renderer.DefaultRenderTargetFactory( rendererClass ); 
			
		m_rt = rtf.createOffscreenRenderTarget();
		m_rt.setSize( 128, 128 );
		m_rt.addCamera( m_camera.getSceneGraphCamera() );
	}
/*
	public static void main( String[] args ) {
		final String srcRootPath = args[ 0 ];
		final String dstRootPath = args[ 1 ];
		
		java.io.File srcDirectory  = new java.io.File( srcRootPath );

		final BatchSaveWithThumbnails batch = new BatchSaveWithThumbnails();
		batch.forEachElement( srcDirectory, new ElementHandler() {
			private java.util.Dictionary m_poseKeyMap = null;
			private java.text.DecimalFormat numberFormatter = new java.text.DecimalFormat( "#0.##" );
			private void outln( String s ) {
				System.out.println( s );
				System.out.flush();
			}

			private org.w3c.dom.Document createCharacterXML( edu.cmu.cs.stage3.alice.core.Transformable model ) {
				javax.xml.parsers.DocumentBuilderFactory factory = null;
				javax.xml.parsers.DocumentBuilder builder = null;
				org.w3c.dom.Document xmlDocument = null;
				try {
					factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
					builder = factory.newDocumentBuilder();
					xmlDocument = builder.newDocument();
				} catch (javax.xml.parsers.ParserConfigurationException pce) {
					pce.printStackTrace();
					return null;
				}
				org.w3c.dom.Element xmlModel = null;

				edu.cmu.cs.stage3.alice.core.util.IndexedTriangleArrayCounter itaCounter = new edu.cmu.cs.stage3.alice.core.util.IndexedTriangleArrayCounter();
				edu.cmu.cs.stage3.alice.core.util.TextureMapCounter textureMapCounter = new edu.cmu.cs.stage3.alice.core.util.TextureMapCounter();

				model.visit( itaCounter, edu.cmu.cs.stage3.util.HowMuch.INSTANCE_AND_ALL_DESCENDANTS );
				model.visit( textureMapCounter, edu.cmu.cs.stage3.util.HowMuch.INSTANCE_AND_ALL_DESCENDANTS );

				xmlModel = xmlDocument.createElement("model");
				org.w3c.dom.Element xmlElement = xmlDocument.createElement("name");
				xmlElement.appendChild( xmlDocument.createTextNode( model.name.getStringValue() ) );
				xmlModel.appendChild(xmlElement);
				if (model.data.get( "modeled by" )!=null && !model.data.get( "modeled by" ).equals("")) {
					xmlElement = xmlDocument.createElement("modeledby");
					xmlElement.appendChild( xmlDocument.createTextNode( model.data.get( "modeled by" ).toString()) );
					xmlModel.appendChild(xmlElement);
				}
				if (model.data.get( "painted by" )!=null && !model.data.get( "painted by" ).equals("")) {
					xmlElement = xmlDocument.createElement("paintedby");
					xmlElement.appendChild( xmlDocument.createTextNode( model.data.get( "painted by" ).toString()) );
					xmlModel.appendChild(xmlElement);
				}
				if (model.data.get( "programmed by" )!=null && !model.data.get( "programmed by" ).equals("")) {
					xmlElement = xmlDocument.createElement("programmedby");
					xmlElement.appendChild( xmlDocument.createTextNode( model.data.get( "programmed by" ).toString()) );
					xmlModel.appendChild(xmlElement);
				}
				xmlElement = xmlDocument.createElement("parts");
				xmlElement.appendChild( xmlDocument.createTextNode(String.valueOf(itaCounter.getIndexedTriangleArrayCount())) );
				xmlModel.appendChild(xmlElement);
				xmlElement = xmlDocument.createElement("physicalsize");
				xmlElement.appendChild( xmlDocument.createTextNode( numberFormatter.format( model.getSize().x ) + "m x " + numberFormatter.format( model.getSize().y ) + "m x " + numberFormatter.format( model.getSize().z ) + "m" ) );
				xmlModel.appendChild(xmlElement);

				org.w3c.dom.Element xmlGroup = xmlDocument.createElement("methods");
				edu.cmu.cs.stage3.alice.core.Element[] listElements = model.responses.getElementArrayValue();
				for (int i=0; i<listElements.length; i++) {
					xmlElement = xmlDocument.createElement("method");
					xmlElement.appendChild(xmlDocument.createTextNode(listElements[i].name.getStringValue()));
					xmlGroup.appendChild(xmlElement);
				}
				xmlModel.appendChild(xmlGroup);

				xmlGroup = xmlDocument.createElement("questions");
				listElements = model.questions.getElementArrayValue();
				for (int i=0; i<listElements.length; i++) {
					xmlElement = xmlDocument.createElement("question");
					xmlElement.appendChild(xmlDocument.createTextNode(listElements[i].name.getStringValue()));
					xmlGroup.appendChild(xmlElement);
				}
				xmlModel.appendChild(xmlGroup);

				xmlGroup = xmlDocument.createElement("sounds");
				listElements = model.sounds.getElementArrayValue();
				for (int i=0; i<listElements.length; i++) {
					xmlElement = xmlDocument.createElement("sound");
					xmlElement.appendChild(xmlDocument.createTextNode(listElements[i].name.getStringValue()));
					xmlGroup.appendChild(xmlElement);
				}
				xmlModel.appendChild(xmlGroup);

				xmlDocument.appendChild( xmlModel );
				xmlDocument.getDocumentElement().normalize();

				return xmlDocument;
			}
			
			private int[] makePixmap(java.awt.Image img) {
				int w = img.getWidth(null);
				int h = img.getHeight(null);
				int[] pixels = new int[w * h];
				java.awt.image.PixelGrabber pg = new java.awt.image.PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
				try {
					pg.grabPixels();
				} catch (InterruptedException e) {
					return null;
				}
				if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
					return null;
				}

				return pixels;
			}

			private void store( edu.cmu.cs.stage3.alice.core.Transformable trans, java.io.File src ) {
				String dstPath = dstRootPath + src.getAbsolutePath().substring( srcRootPath.length() );
				java.io.File dstParent = new java.io.File( dstPath ).getParentFile();

				if( dstParent.exists() ) {
					//pass
				} else {
					dstParent.mkdirs();
					outln( "creating directories for: " + dstParent );
				}

				String elementName = trans.name.getStringValue();
				String fileName = Character.toUpperCase( elementName.charAt( 0 ) ) + elementName.substring( 1 ) + ".a2c"; 
				java.io.File dst = new java.io.File( dstParent, fileName );

				trans.vehicle.set( batch.getWorld() );

				java.util.Dictionary map = new java.util.Hashtable();
				batch.m_camera.getAGoodLookAtRightNow( trans );

				edu.cmu.cs.stage3.math.Sphere bs = trans.getBoundingSphere();
				if( (bs != null) && (bs.getCenter() != null) && (bs.getRadius() > 0.0) ) {
					double radius = bs.getRadius();
					double theta = Math.min( batch.m_camera.horizontalViewingAngle.doubleValue(), batch.m_camera.verticalViewingAngle.doubleValue() );
					double farDist = radius/Math.sin( theta/2.0 ) + radius;
					batch.m_camera.farClippingPlaneDistance.set( new Double( farDist ) );
				}

//				if( batch.m_rt.getRenderer() instanceof edu.cmu.cs.stage3.alice.scenegraph.renderer.directx7renderer.Renderer ) {
//					((edu.cmu.cs.stage3.alice.scenegraph.renderer.directx7