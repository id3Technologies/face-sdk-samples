namespace id3.Face.Samples.PadWF
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
            this.pictureBoxPreview = new System.Windows.Forms.PictureBox();
            this.buttonStartCapture = new System.Windows.Forms.Button();
            this.buttonComputePad = new System.Windows.Forms.Button();
            this.labelColorPadScore = new System.Windows.Forms.Label();
            this.labelBlurrinessScore = new System.Windows.Forms.Label();
            this.labelAttackSupportScore = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxPreview)).BeginInit();
            this.SuspendLayout();
            // 
            // pictureBoxPreview
            // 
            this.pictureBoxPreview.Location = new System.Drawing.Point(42, 12);
            this.pictureBoxPreview.Name = "pictureBoxPreview";
            this.pictureBoxPreview.Size = new System.Drawing.Size(640, 480);
            this.pictureBoxPreview.TabIndex = 0;
            this.pictureBoxPreview.TabStop = false;
            // 
            // buttonStartCapture
            // 
            this.buttonStartCapture.Location = new System.Drawing.Point(42, 530);
            this.buttonStartCapture.Name = "buttonStartCapture";
            this.buttonStartCapture.Size = new System.Drawing.Size(159, 73);
            this.buttonStartCapture.TabIndex = 1;
            this.buttonStartCapture.Text = "Start capture";
            this.buttonStartCapture.UseVisualStyleBackColor = true;
            // 
            // buttonComputePad
            // 
            this.buttonComputePad.Location = new System.Drawing.Point(286, 530);
            this.buttonComputePad.Name = "buttonComputePad";
            this.buttonComputePad.Size = new System.Drawing.Size(159, 73);
            this.buttonComputePad.TabIndex = 2;
            this.buttonComputePad.Text = "Compute PAD";
            this.buttonComputePad.UseVisualStyleBackColor = true;
            // 
            // labelColorPadScore
            // 
            this.labelColorPadScore.AutoSize = true;
            this.labelColorPadScore.Location = new System.Drawing.Point(735, 100);
            this.labelColorPadScore.Name = "labelColorPadScore";
            this.labelColorPadScore.Size = new System.Drawing.Size(91, 13);
            this.labelColorPadScore.TabIndex = 3;
            this.labelColorPadScore.Text = "Color PAD score: ";
            // 
            // labelBlurrinessScore
            // 
            this.labelBlurrinessScore.AutoSize = true;
            this.labelBlurrinessScore.Location = new System.Drawing.Point(735, 126);
            this.labelBlurrinessScore.Name = "labelBlurrinessScore";
            this.labelBlurrinessScore.Size = new System.Drawing.Size(87, 13);
            this.labelBlurrinessScore.TabIndex = 5;
            this.labelBlurrinessScore.Text = "Blurriness score: ";
            // 
            // labelAttackSupportScore
            // 
            this.labelAttackSupportScore.AutoSize = true;
            this.labelAttackSupportScore.Location = new System.Drawing.Point(735, 139);
            this.labelAttackSupportScore.Name = "labelAttackSupportScore";
            this.labelAttackSupportScore.Size = new System.Drawing.Size(111, 13);
            this.labelAttackSupportScore.TabIndex = 6;
            this.labelAttackSupportScore.Text = "Attack support score: ";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(947, 648);
            this.Controls.Add(this.labelAttackSupportScore);
            this.Controls.Add(this.labelBlurrinessScore);
            this.Controls.Add(this.labelColorPadScore);
            this.Controls.Add(this.buttonComputePad);
            this.Controls.Add(this.buttonStartCapture);
            this.Controls.Add(this.pictureBoxPreview);
            this.Name = "Form1";
            this.Text = "id3Face Capture Sample";
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxPreview)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox pictureBoxPreview;
        private System.Windows.Forms.Button buttonStartCapture;
        private System.Windows.Forms.Button buttonComputePad;
        private System.Windows.Forms.Label labelColorPadScore;
        private System.Windows.Forms.Label labelBlurrinessScore;
        private System.Windows.Forms.Label labelAttackSupportScore;
    }
}

