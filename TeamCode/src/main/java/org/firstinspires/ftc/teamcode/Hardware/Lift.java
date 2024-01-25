package org.firstinspires.ftc.teamcode.Hardware;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad2;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.LibraryFiles.Contraption;

@Config
public class Lift extends Contraption {
    DcMotor LeftLift, RightLift;
    public Lift(LinearOpMode opMode) {
        this.opMode = opMode;
    }
    @Override
    public void initialize(HardwareMap hwMap) {
        RightLift = hwMap.get(DcMotorEx.class, "rightArm");
        LeftLift = hwMap.get(DcMotorEx.class, "leftArm");
    }

    public void loop(Gamepad gamepad) {
        if (gamepad2.left_stick_y < 0) {
            // up
            LeftLift.setPower(1);
            RightLift.setPower(1);
        } else if (gamepad2.left_stick_y > 0) {
            // down
            LeftLift.setPower(-1);
            RightLift.setPower(-1);
        } else {
            LeftLift.setPower(0);
            RightLift.setPower(0);
        }
    }
}
