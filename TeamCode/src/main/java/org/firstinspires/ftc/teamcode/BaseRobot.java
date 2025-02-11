/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class BaseRobot extends OpMode {


    public DcMotor leftBackDriveMotor, rightBackDriveMotor, leftFrontDriveMotor, rightFrontDriveMotor, armMotor1, armMotor2, baseMoverMotor;
    public Servo claw_servo_1, claw_servo_2;
    public ElapsedTime timer = new ElapsedTime();

    public void init() {
        leftBackDriveMotor = hardwareMap.get(DcMotor.class, "leftBackDriveMotor");
        rightBackDriveMotor = hardwareMap.get(DcMotor.class, "rightBackDriveMotor");
        leftFrontDriveMotor = hardwareMap.get(DcMotor.class, "leftFrontDriveMotor");
        rightFrontDriveMotor = hardwareMap.get(DcMotor.class, "rightFrontDriveMotor");

        armMotor1 = hardwareMap.get(DcMotor.class, "armMotor1");
        armMotor2 = hardwareMap.get(DcMotor.class, "armMotor2");

        baseMoverMotor = hardwareMap.get(DcMotor.class, "baseMoverMotor");

        claw_servo_1 = hardwareMap.get(Servo.class, "claw_servo_1");
        claw_servo_2 = hardwareMap.get(Servo.class, "claw_servo_2");

        set_claw_servo_1(ConstantVariables.K_CLAW_SERVO_1_OPEN);
        set_claw_servo_2(ConstantVariables.K_CLAW_SERVO_2_OPEN);
    }

    public void start() {
        timer.reset();
        reset_drive_encoders();
        reset_arm_encoders();
        reset_base_mover_encoder();
    }

    public void loop() {
        telemetry.addData("D00 Left Front Drive Motor Enc: ", get_left_front_drive_motor_enc());
        telemetry.addData("D01 Right Front Drive Motor Enc: ", get_right_front_drive_motor_enc());
        telemetry.addData("D02 Left Back Drive Motor Enc: ", get_left_back_drive_motor_enc());
        telemetry.addData("D03 Right Back Drive Motor Enc: ", get_right_back_drive_motor_enc());
        telemetry.addData("D04 Arm Motor 1 Enc: ", get_arm_motor_1_enc());
        telemetry.addData("D05 Arm Motor 2 Enc: ", get_arm_motor_2_enc());
        telemetry.addData("D06 Base Mover Motor Enc: ", get_base_mover_enc());
        telemetry.addData("D06 Claw Servo 1 Pos: ", claw_servo_1.getPosition());
        telemetry.addData("D07 Claw Servo 2 Pos: ", claw_servo_2.getPosition());
    }


    public boolean auto_drive(double power, double inches) {
        double TARGET_ENC = ConstantVariables.K_PPIN_DRIVE * inches;
        telemetry.addData("Target_enc: ", TARGET_ENC);
        double left_speed = -power;
        double right_speed = power;
        /*double error = -get_left_front_drive_motor_enc() - get_right_front_drive_motor_enc();
        error /= ConstantVariables.K_DRIVE_ERROR_P;
        left_speed += error;
        right_speed -= error;*/

        left_speed = Range.clip(left_speed, -1, 1);
        right_speed = Range.clip(right_speed, -1, 1);
        leftFrontDriveMotor.setPower(left_speed);
        leftBackDriveMotor.setPower(left_speed);
        rightFrontDriveMotor.setPower(right_speed);
        rightBackDriveMotor.setPower(right_speed);

        if (Math.abs(get_right_front_drive_motor_enc()) >= TARGET_ENC) {
            leftFrontDriveMotor.setPower(0);
            leftBackDriveMotor.setPower(0);
            rightFrontDriveMotor.setPower(0);
            rightBackDriveMotor.setPower(0);
            return true;
        }
        return false;
    }

    /**
     * @param power:   the speed to turn at. Negative for left.
     * @param degrees: the number of degrees to turn.
     * @return Whether the target angle has been reached.
     */

    public boolean auto_turn(double power, double degrees) {
        double TARGET_ENC = Math.abs(ConstantVariables.K_PPDEG_DRIVE * degrees);
        telemetry.addData("D99 TURNING TO ENC: ", TARGET_ENC);

        double speed = Range.clip(power, -1, 1);
        leftFrontDriveMotor.setPower(-speed);
        leftBackDriveMotor.setPower(-speed);
        rightFrontDriveMotor.setPower(-speed);
        rightBackDriveMotor.setPower(-speed);

        if (Math.abs(get_right_front_drive_motor_enc()) >= TARGET_ENC) {
            leftFrontDriveMotor.setPower(0);
            leftBackDriveMotor.setPower(0);
            rightFrontDriveMotor.setPower(0);
            rightBackDriveMotor.setPower(0);
            return true;
        } else {
            return false;
        }
    }

    //positive for right, negative for left
    public boolean auto_mecanum(double power, double inches) {
        double TARGET_ENC = ConstantVariables.K_PPIN_DRIVE * inches;
        telemetry.addData("Target_enc: ", TARGET_ENC);

        double leftFrontPower = Range.clip(0 - power, -1.0, 1.0);
        double leftBackPower = Range.clip(0 + power, -1.0, 1.0);
        double rightFrontPower = Range.clip(0 - power, -1.0, 1.0);
        double rightBackPower = Range.clip(0 + power, -1.0, 1.0);

        leftFrontDriveMotor.setPower(leftFrontPower);
        leftBackDriveMotor.setPower(leftBackPower);
        rightFrontDriveMotor.setPower(rightFrontPower);
        rightBackDriveMotor.setPower(rightBackPower);

        if (Math.abs(get_right_front_drive_motor_enc()) >= TARGET_ENC) {
            leftFrontDriveMotor.setPower(0);
            leftBackDriveMotor.setPower(0);
            rightFrontDriveMotor.setPower(0);
            rightBackDriveMotor.setPower(0);
            return true;
        } else {
            return false;
        }
    }

    public void tankanum_drive(double rightPwr, double leftPwr, double lateralpwr) {
        rightPwr *= -1;

        double leftFrontPower = Range.clip(leftPwr - lateralpwr, -1.0, 1.0);
        double leftBackPower = Range.clip(leftPwr + lateralpwr, -1.0, 1.0);
        double rightFrontPower = Range.clip(rightPwr - lateralpwr, -1.0, 1.0);
        double rightBackPower = Range.clip(rightPwr + lateralpwr, -1.0, 1.0);

        leftFrontDriveMotor.setPower(leftFrontPower);
        leftBackDriveMotor.setPower(leftBackPower);
        rightFrontDriveMotor.setPower(rightFrontPower);
        rightBackDriveMotor.setPower(rightBackPower);
    }
    public void arm1(double power) {
        double speed = Range.clip(power, -1, 1);
        armMotor1.setPower(speed);
    }
    public void arm2(double power) {
        double speed = Range.clip(power, -1, 1);
        armMotor2.setPower(speed);
    }

    public void base_mover(double power) {
        double speed = Range.clip(power, -1, 1);
        baseMoverMotor.setPower(speed);
    }

    public void set_claw_servo_1(double pos) {
        double position = Range.clip(pos, 0, 1.0);
        claw_servo_1.setPosition(position);
    }

    public void set_claw_servo_2(double pos) {
        double position = Range.clip(pos, 0, 1.0);
        claw_servo_2.setPosition(position);
    }

    public void reset_drive_encoders() {
        leftFrontDriveMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDriveMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDriveMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDriveMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFrontDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void reset_base_mover_encoder() {
        baseMoverMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        baseMoverMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void reset_arm_encoders() {
        armMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        armMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        armMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public int get_left_front_drive_motor_enc() {
        if (leftFrontDriveMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            leftFrontDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        return leftFrontDriveMotor.getCurrentPosition();
    }

    public int get_right_front_drive_motor_enc() {
        if (rightFrontDriveMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            rightFrontDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        return rightFrontDriveMotor.getCurrentPosition();
    }

    public int get_left_back_drive_motor_enc() {
        if (leftBackDriveMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            leftBackDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        return leftBackDriveMotor.getCurrentPosition();
    }

    public int get_right_back_drive_motor_enc() {
        if (rightBackDriveMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            rightBackDriveMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        return rightBackDriveMotor.getCurrentPosition();
    }

    public int get_arm_motor_1_enc() {
        if (armMotor1.getMode() !=DcMotor.RunMode.RUN_USING_ENCODER) {
            armMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        return armMotor1.getCurrentPosition();
    }

    public int get_arm_motor_2_enc() {
        if (armMotor2.getMode() !=DcMotor.RunMode.RUN_USING_ENCODER) {
            armMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        return armMotor2.getCurrentPosition();
    }

    public int get_base_mover_enc() {
        if (baseMoverMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            baseMoverMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        return baseMoverMotor.getCurrentPosition();
    }


}





