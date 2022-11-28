// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.cameraserver.CameraServer;

//CAMERAO CODE;
//import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSink;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.cscore.UsbCamera;
//import edu.wpi.first.wpilibj.TimedRobot;
import org.opencv.core.Mat;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot 
{
  //
  private final WPI_TalonFX talon = new WPI_TalonFX(1, "CANivore");

  private final XboxController Ctrl = new XboxController(0);
  private final CANSparkMax SparkMas = new CANSparkMax(0, MotorType.kBrushless); 

  //Pneumatics
  private final Compressor NewComp = new Compressor(0, PneumaticsModuleType.CTREPCM);
  private final DoubleSolenoid SOLO = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);

  //Cam Stuff
  Thread m_visionThread;
  
  //MjpegServer JpegServer = new MjpegServer("serve_USB Camera 0", 1181);
  //CvSink cvsink = new CvSink("opencv_USB Camera 0");
  //CvSource OutputStream = new CvSource("Blur", PixelFormat.kMJPEG, 640, 480, 30);
  //MjpegServer MjpegServer2 = new MjpegServer("serve_Blur", 1182);
  //End
  

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() 
  {
    NewComp.enableAnalog(0, 10);

    //SOLO.set(Value.kOff);
    SOLO.set(Value.kReverse);
    //SOLO.set(Value.kForward);

    talon.set(ControlMode.PercentOutput, 0);

    m_visionThread = new Thread(
    () -> 
    {
      // Get the UsbCamera from CameraServer
      UsbCamera camera = CameraServer.startAutomaticCapture();
      // Set the resolution
      camera.setResolution(640, 480);

      // Get a CvSink. This will capture Mats from the camera
      CvSink cvSink = CameraServer.getVideo();
      // Setup a CvSource. This will send images back to the Dashboard
      CvSource outputStream = CameraServer.putVideo("Rectangle", 640, 480);

      Mat mat = new Mat();

      // This cannot be 'true'. The program will never exit if it is. This
      // lets the robot stop this thread when restarting robot code or
      // deploying.
      while (!Thread.interrupted()) 
      {
        // Tell the CvSink to grab a frame from the camera and put it
        // in the source mat.  If there is an error notify the output.
        if (cvSink.grabFrame(mat) == 0) 
        {
          // Send the output the error.
          outputStream.notifyError(cvSink.getError());
          // skip the rest of the current iteration
          continue;
        }
      }
    });
    m_visionThread.setDaemon(true);
    m_visionThread.start();
  }

  @Override
  public void robotPeriodic() 
  {
    if (Ctrl.getRawButton(1))
    {
      talon.set(ControlMode.PercentOutput, 0.2);
    }
    else if (Ctrl.getRawButton(3))
    {
      talon.set(ControlMode.PercentOutput, Ctrl.getLeftY());  
      System.out.println(talon.getSelectedSensorVelocity(1));
      
    }
    else 
    {
      talon.set(ControlMode.PercentOutput, 0);
    }

    //SparkMax
    if (Ctrl.getRawButton(2))
    {
      SparkMas.set(0.3);
    }
    else
    {
      SparkMas.set(0);
    }

    //Pneumatics
    if (Ctrl.getRawButtonPressed(5))
    {
      SOLO.toggle();
    }
    else if (Ctrl.getRawButtonReleased(5))
    {
      SOLO.toggle();
    }

  }

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {}

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
}


