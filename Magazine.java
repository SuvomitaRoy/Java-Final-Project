public class Magazine extends Document {
        private Integer number;
        public enum Periodicity {
        DAILY,
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        BIMONTHLY,
        QUARTERLY,
        YEARLY
        }
        private Periodicity periodicity;

        public Magazine(String title, String author, String genre, Integer number, Periodicity periodicity){
            super(title, author, genre);
            this.number = number;
            this.periodicity = periodicity;
        }

        public Integer getNumber(){
            return(this.number);
        }

        public Periodicity getPeriodicity(){
            return(this.periodicity);
        }

        public void setNumber(Integer number){
            this.number = number;
        }
        public void setPeriodicity(Periodicity periodicity){
            this.periodicity = periodicity;
        }
}