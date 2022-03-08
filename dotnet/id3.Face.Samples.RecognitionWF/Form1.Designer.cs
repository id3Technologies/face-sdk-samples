namespace id3.Face.Samples.RecognitionWF
{
    partial class Form1
    {
        /// <summary>
        /// Variable nécessaire au concepteur.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Nettoyage des ressources utilisées.
        /// </summary>
        /// <param name="disposing">true si les ressources managées doivent être supprimées ; sinon, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Code généré par le Concepteur Windows Form

        /// <summary>
        /// Méthode requise pour la prise en charge du concepteur - ne modifiez pas
        /// le contenu de cette méthode avec l'éditeur de code.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            this.pictureBoxPreview = new System.Windows.Forms.PictureBox();
            this.buttonStartCapture = new System.Windows.Forms.Button();
            this.buttonEnroll = new System.Windows.Forms.Button();
            this.buttonMatch = new System.Windows.Forms.Button();
            this.pictureBoxCandidate = new System.Windows.Forms.PictureBox();
            this.pictureBoxEnrollee = new System.Windows.Forms.PictureBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.labelMatchScore = new System.Windows.Forms.Label();
            this.labelDetectionTime = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxPreview)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxCandidate)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxEnrollee)).BeginInit();
            this.SuspendLayout();
            // 
            // pictureBoxPreview
            // 
            this.pictureBoxPreview.Location = new System.Drawing.Point(56, 15);
            this.pictureBoxPreview.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.pictureBoxPreview.Name = "pictureBoxPreview";
            this.pictureBoxPreview.Size = new System.Drawing.Size(853, 591);
            this.pictureBoxPreview.TabIndex = 0;
            this.pictureBoxPreview.TabStop = false;
            // 
            // buttonStartCapture
            // 
            this.buttonStartCapture.Location = new System.Drawing.Point(56, 652);
            this.buttonStartCapture.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.buttonStartCapture.Name = "buttonStartCapture";
            this.buttonStartCapture.Size = new System.Drawing.Size(212, 90);
            this.buttonStartCapture.TabIndex = 1;
            this.buttonStartCapture.Text = "Start capture";
            this.buttonStartCapture.UseVisualStyleBackColor = true;
            // 
            // buttonEnroll
            // 
            this.buttonEnroll.Location = new System.Drawing.Point(381, 652);
            this.buttonEnroll.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.buttonEnroll.Name = "buttonEnroll";
            this.buttonEnroll.Size = new System.Drawing.Size(212, 90);
            this.buttonEnroll.TabIndex = 2;
            this.buttonEnroll.Text = "Enroll";
            this.buttonEnroll.UseVisualStyleBackColor = true;
            // 
            // buttonMatch
            // 
            this.buttonMatch.Location = new System.Drawing.Point(697, 652);
            this.buttonMatch.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.buttonMatch.Name = "buttonMatch";
            this.buttonMatch.Size = new System.Drawing.Size(212, 90);
            this.buttonMatch.TabIndex = 3;
            this.buttonMatch.Text = "Match";
            this.buttonMatch.UseVisualStyleBackColor = true;
            // 
            // pictureBoxCandidate
            // 
            this.pictureBoxCandidate.Image = ((System.Drawing.Image)(resources.GetObject("pictureBoxCandidate.Image")));
            this.pictureBoxCandidate.Location = new System.Drawing.Point(987, 343);
            this.pictureBoxCandidate.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.pictureBoxCandidate.Name = "pictureBoxCandidate";
            this.pictureBoxCandidate.Size = new System.Drawing.Size(200, 246);
            this.pictureBoxCandidate.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBoxCandidate.TabIndex = 5;
            this.pictureBoxCandidate.TabStop = false;
            // 
            // pictureBoxEnrollee
            // 
            this.pictureBoxEnrollee.Image = ((System.Drawing.Image)(resources.GetObject("pictureBoxEnrollee.Image")));
            this.pictureBoxEnrollee.InitialImage = null;
            this.pictureBoxEnrollee.Location = new System.Drawing.Point(987, 15);
            this.pictureBoxEnrollee.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.pictureBoxEnrollee.Name = "pictureBoxEnrollee";
            this.pictureBoxEnrollee.Size = new System.Drawing.Size(200, 246);
            this.pictureBoxEnrollee.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBoxEnrollee.TabIndex = 6;
            this.pictureBoxEnrollee.TabStop = false;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(983, 265);
            this.label1.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(60, 17);
            this.label1.TabIndex = 7;
            this.label1.Text = "Enrollee";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(983, 593);
            this.label2.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(72, 17);
            this.label2.TabIndex = 8;
            this.label2.Text = "Candidate";
            // 
            // labelMatchScore
            // 
            this.labelMatchScore.AutoSize = true;
            this.labelMatchScore.Location = new System.Drawing.Point(983, 726);
            this.labelMatchScore.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.labelMatchScore.Name = "labelMatchScore";
            this.labelMatchScore.Size = new System.Drawing.Size(98, 17);
            this.labelMatchScore.TabIndex = 9;
            this.labelMatchScore.Text = "Match score: -";
            // 
            // labelDetectionTime
            // 
            this.labelDetectionTime.AutoSize = true;
            this.labelDetectionTime.Location = new System.Drawing.Point(983, 652);
            this.labelDetectionTime.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.labelDetectionTime.Name = "labelDetectionTime";
            this.labelDetectionTime.Size = new System.Drawing.Size(111, 17);
            this.labelDetectionTime.TabIndex = 10;
            this.labelDetectionTime.Text = "Detection time: -";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1263, 798);
            this.Controls.Add(this.labelDetectionTime);
            this.Controls.Add(this.labelMatchScore);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.pictureBoxEnrollee);
            this.Controls.Add(this.pictureBoxCandidate);
            this.Controls.Add(this.buttonMatch);
            this.Controls.Add(this.buttonEnroll);
            this.Controls.Add(this.buttonStartCapture);
            this.Controls.Add(this.pictureBoxPreview);
            this.Margin = new System.Windows.Forms.Padding(4, 4, 4, 4);
            this.Name = "Form1";
            this.Text = "id3Face Recognition Sample";
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxPreview)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxCandidate)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxEnrollee)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox pictureBoxPreview;
        private System.Windows.Forms.Button buttonStartCapture;
        private System.Windows.Forms.Button buttonEnroll;
        private System.Windows.Forms.Button buttonMatch;
        private System.Windows.Forms.PictureBox pictureBoxCandidate;
        private System.Windows.Forms.PictureBox pictureBoxEnrollee;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label labelMatchScore;
        private System.Windows.Forms.Label labelDetectionTime;
    }
}

