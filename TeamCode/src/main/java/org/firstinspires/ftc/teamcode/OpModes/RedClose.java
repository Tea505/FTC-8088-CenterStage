package org.firstinspires.ftc.teamcode.OpModes;

import static org.firstinspires.ftc.teamcode.LibraryFiles.Constants.MaxAccel;
import static org.firstinspires.ftc.teamcode.LibraryFiles.Constants.MaxVel;
import static org.firstinspires.ftc.teamcode.LibraryFiles.Constants.Side.CENTER;
import static org.firstinspires.ftc.teamcode.LibraryFiles.Constants.Side.LEFT;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.Hardware.Intake;
import org.firstinspires.ftc.teamcode.Hardware.Lift;
import org.firstinspires.ftc.teamcode.Hardware.Wrist;
import org.firstinspires.ftc.teamcode.LibraryFiles.Constants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.List;
@Autonomous
public class RedClose extends LinearOpMode {

    public VisionPortal myVisionPortal;
    public TfodProcessor myTfodProcessor;
 +   public boolean USE_WEBCAM;
    public double proplocation;
    SampleMecanumDrive drive;
    Lift lift = new Lift(this);
    Intake claw = new Intake(this);
    Wrist wrist = new Wrist(this);

    @Override
    public void runOpMode() throws InterruptedException {
        USE_WEBCAM = true;
        initTfod();

        drive = new SampleMecanumDrive(hardwareMap);
        lift.initialize(hardwareMap);
        claw.initialize(hardwareMap);

        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));
+
        while (!isStarted()) {
            proplocation = Tfod_location();
            telemetry.addData("location: ", proplocation);
            telemetry.update();
        }

        drive.setPoseEstimate(startPose);
        // these have not been tested they are just base line
        TrajectorySequence Centered = drive.trajectorySequenceBuilder(startPose)
                .setConstraints(MaxVel, MaxAccel)

                .forward(30)
                //open one claw here
                .addTemporalMarker(() -> claw.OpenRight())
                .waitSeconds(0.8)
                // need refining
                .back(5)
                .turn(Math.toRadians(90))
                .back(29)
                .addTemporalMarker(()-> lift.autoMove(1250,.8))
                .waitSeconds(1)

                .addTemporalMarker(()-> Wrist.wrist. setPosition(0))
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> claw.OpenLeft())
                .forward(4.5)
                .addTemporalMarker(() -> lift.autoMove(1250, 0.65))
                .strafeLeft(12)
                //.addTemporalMarker(()-> claw.OpenLeft())
                //going at add spline later
                //.waitSeconds(2)
                //.strafeLeft(20)
                .build();

        TrajectorySequence Left = drive.trajectorySequenceBuilder(startPose)
                .setConstraints(MaxVel, MaxAccel)

                .forward(29)
                .turn(Math.toRadians(90))
                .strafeLeft(12)
                //open one claw here
                .waitSeconds(2)
                .back(18)
                //score thingy here
                .strafeLeft(10)
                .build();

        TrajectorySequence Right = drive.trajectorySequenceBuilder(startPose)
                .setConstraints(MaxVel, MaxAccel)

                .forward(29)
                .turn(Math.toRadians(-90))
                //open one Claw here
                .waitSeconds(2)
                .back(5)
                .strafeRight(6)
                //Going to do spline on actual field for auccarcy
                .back(18)
                .waitSeconds(2)
                .strafeRight(16)
                //scoring thingy
                .build();

        waitForStart();
        myVisionPortal.close();

        if (isStopRequested()) return;

        if (proplocation == 1) {
            drive.followTrajectorySequence(Centered);
        } else if (proplocation == 2) {
            drive.followTrajectorySequence(Left);
        } else {
            drive.followTrajectorySequence(Right);

        }
    }


    /**
     * Describe this function...
     */
    private double Tfod_location() {
        List<Recognition> myTfodRecognitions;
        Recognition myTfodRecognition;
        float x;
        float y;

        // Get a list of recognitions from TFOD.
        myTfodRecognitions = myTfodProcessor.getRecognitions();
        telemetry.addData("# Objects Detected", JavaUtil.listLength(myTfodRecognitions));
        if (JavaUtil.listLength(myTfodRecognitions) == 0) {
            proplocation = 0;
        } else {
            // Iterate through list and call a function to display info for each recognized object.
            for (Recognition myTfodRecognition_item : myTfodRecognitions) {
                myTfodRecognition = myTfodRecognition_item;
                // Display info about the recognition.
                telemetry.addLine("");
                // Display label and confidence.
                // Display the label and confidence for the recognition.
                telemetry.addData("Image", myTfodRecognition.getLabel() + " (" + JavaUtil.formatNumber(myTfodRecognition.getConfidence() * 100, 0) + " % Conf.)");
                // Display position.
                x = (myTfodRecognition.getLeft() + myTfodRecognition.getRight()) / 2;
                y = (myTfodRecognition.getTop() + myTfodRecognition.getBottom()) / 2;
                // Display the position of the center of the detection boundary for the recognition
                telemetry.addData("- Position", JavaUtil.formatNumber(x, 0) + ", " + JavaUtil.formatNumber(y, 0));
                if (x >= 100) {
                    proplocation = 1;
                    telemetry.addLine("CENTERED");
                } else if (x <= 80) {
                    proplocation = 2;
                    telemetry.addLine("LEFT");
                } else {
                    proplocation = 3;
                }
                // Display size
                // Display the size of detection boundary for the recognition
                telemetry.addData("- Size", JavaUtil.formatNumber(myTfodRecognition.getWidth(), 0) + " x " + JavaUtil.formatNumber(myTfodRecognition.getHeight(), 0));
            }

        }
        return proplocation;
    }

    private void initTfod() {
        TfodProcessor.Builder myTfodProcessorBuilder;
        VisionPortal.Builder myVisionPortalBuilder;

        // First, create a TfodProcessor.Builder.
        myTfodProcessorBuilder = new TfodProcessor.Builder();
        // Set the name of the file where the model can be found.
        myTfodProcessorBuilder.setModelFileName("8088_red_model_metadata.tflite");
        // Set the full ordered list of labels the model is trained to recognize.
        myTfodProcessorBuilder.setModelLabels(JavaUtil.createListWith("None", "Red Prop"));
        // Set the aspect ratio for the images used when the model was created.
        myTfodProcessorBuilder.setModelAspectRatio(16 / 9);
        // Create a TfodProcessor by calling build.
        myTfodProcessor = myTfodProcessorBuilder.build();
        // Next, create a VisionPortal.Builder and set attributes related to the camera.
        myVisionPortalBuilder = new VisionPortal.Builder();
        if (USE_WEBCAM) {
            // Use a webcam.
            myVisionPortalBuilder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        } else {
            // Use the device's back camera.
            myVisionPortalBuilder.setCamera(BuiltinCameraDirection.BACK);
        }
        // Add myTfodProcessor to the VisionPortal.Builder.
        myVisionPortalBuilder.addProcessor(myTfodProcessor);
        // Create a VisionPortal by calling build.
        myVisionPortal = myVisionPortalBuilder.build();



    }
}
