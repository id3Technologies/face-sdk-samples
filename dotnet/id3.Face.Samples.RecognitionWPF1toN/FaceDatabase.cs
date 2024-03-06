using id3.Face;
using System.Collections.ObjectModel;
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

            FaceIndexer = FaceIndexer.Create(maximumTemplateCount, format);
        }

        public static void Add(string key, FaceTemplate faceTemplateItem)
        {
            ReferenceTemplateList.Add(key, faceTemplateItem);
            FaceIndexer.AddTemplate(faceTemplateItem, key);
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

        public static FaceCandidateList SearchTemplate(FaceTemplate probe, int maxCandidates)
        {
            return FaceIndexer.SearchTemplate(probe, maxCandidates);
        }
    }
}
