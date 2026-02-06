using id3.Face;
using System;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Data;

namespace id3FaceSearchSampleWPF
{
    public static class FaceDatabase
    {
        // lock object for the reference lists (ReferenceTemplateList and ReferenceDataList)
        public static object ReferenceListLock = new object();
        public static FaceTemplateDict ReferenceTemplateList { get; private set; }
        public static ObservableCollection<FaceDatabaseItem> ReferenceDataList { get; set; }

        public static FaceIndexer FaceIndexer { get; private set; }

        public static void Initialize(int maximumTemplateCount, FaceTemplateFormat format)
        {
            ReferenceTemplateList = new FaceTemplateDict();
            ReferenceDataList = new ObservableCollection<FaceDatabaseItem>();
            BindingOperations.EnableCollectionSynchronization(ReferenceDataList, ReferenceListLock);

            try
            {
                FaceIndexer = FaceIndexer.Create(maximumTemplateCount, format);
            }
            catch (FaceException ex)
            {
                MessageBox.Show(string.Format("FaceIndexer.Create FaceException: {0}", ex.Message));
            }
        }

        public static void Add(int key, FaceTemplate faceTemplateItem)
        {
            ReferenceTemplateList.Add(key, faceTemplateItem);
            FaceIndexer.Add(faceTemplateItem, new Guid(key, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).ToByteArray());
        }

        public static int Count
        {
            get { return ReferenceTemplateList.Count; }
        }

        public static FaceTemplateFormat Format
        {
            get
            {
                return FaceIndexer.Format;
            }
        }

        public static FaceCandidateIndexerList SearchTemplate(FaceTemplate probe, int maxCandidates)
        {
            return FaceIndexer.Search(probe, maxCandidates);
        }
    }
}
