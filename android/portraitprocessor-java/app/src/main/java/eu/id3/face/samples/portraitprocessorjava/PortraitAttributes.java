package eu.id3.face.samples.portraitprocessorjava;

import eu.id3.face.BackgroundUniformity;
import eu.id3.face.FaceExpression;
import eu.id3.face.FacePose;

public class PortraitAttributes {

    private int age_;
    private FaceExpression expression_;
    private int genderMaleScore_;

    private FacePose pose_;
    private int leftEyeVisiblityScore_;
    private int rightEyeVisiblityScore_;
    private int mouthVisiblityScore_;
    private int noseVisiblityScore_;
    private int glassesVisiblityScore_;
    private int hatScore_;
    private int mouthOpenScore_;
    private int smileScore_;

    private int qualityScore_;

    public PortraitAttributes() {

        age_ = -1;
        expression_ = FaceExpression.UNKNOWN;
        genderMaleScore_ = -1;

        pose_ = new FacePose();
        leftEyeVisiblityScore_ = -1;
        rightEyeVisiblityScore_ = -1;
        mouthVisiblityScore_ = -1;
        noseVisiblityScore_ = -1;
        glassesVisiblityScore_ = -1;
        hatScore_ = -1;
        mouthOpenScore_ = -1;
        smileScore_ = -1;

        qualityScore_ = -1;
    }

    public void setAge(int value) {
        age_ = value;
    }
    public int getAge() {
        return age_;
    }

    public void setExpression(FaceExpression value) {
        expression_ = value;
    }
    public FaceExpression getExpression() {
        return expression_;
    }

    public void setGenderMaleScore(int value) {
        genderMaleScore_ = value;
    }
    public int getGenderMaleScore() {
        return genderMaleScore_;
    }

    public void setPose(FacePose value) {
        pose_ = value;
    }
    public FacePose getPose() {
        return pose_;
    }

    public void setLeftEyeVisibilityScore(int value) {
        leftEyeVisiblityScore_ = value;
    }
    public int getLeftEyeVisibilityScore() {
        return leftEyeVisiblityScore_;
    }

    public void setRightEyeVisibilityScore(int value) {
        rightEyeVisiblityScore_ = value;
    }
    public int getRightEyeVisibilityScore() {
        return rightEyeVisiblityScore_;
    }

    public void setMouthVisibilityScore(int value) {
        mouthVisiblityScore_ = value;
    }
    public int getMouthVisibilityScore() {
        return mouthVisiblityScore_;
    }

    public void setNoseVisibilityScore(int value) {
        noseVisiblityScore_ = value;
    }
    public int getNoseVisibilityScore() {
        return noseVisiblityScore_;
    }

    public void setGlassesVisibilityScore(int value) {
        glassesVisiblityScore_ = value;
    }
    public int getGlassesVisibilityScore() {
        return glassesVisiblityScore_;
    }

    public void setHatScore(int value) {
        hatScore_ = value;
    }
    public int getHatScore() {
        return hatScore_;
    }

    public void setMouthOpenScore(int value) {
        mouthOpenScore_ = value;
    }
    public int getMouthOpenScore() {
        return mouthOpenScore_;
    }

    public void setSmileScore(int value) {
        smileScore_ = value;
    }
    public int getSmileScore() {
        return smileScore_;
    }

    public void setQualityScore(int value) {
        qualityScore_ = value;
    }
    public int getQualityScore() {
        return qualityScore_;
    }
}
