package tempest.hbase;

import java.text.SimpleDateFormat;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.rightPad;

public enum HBFormatter {

    NONE("") {
        public String format(Object target) {
            return nonNull(target) ? target.toString() : null;
        }
    },

    CPF("00000000000") {
        public String format(Object target) {
            return nonNull(target) ?
                    rightPad(target.toString(), format.length(), ' ') : null;
        }
    },

    MF_DATE("dd-MM-yyyy") {
        public String format(Object target) {
            return HBFormatter.dateFormat(format, target);
        }
    },
    MF_TIME("HH:mm:ss") {
        public String format(Object target) {
            return HBFormatter.dateFormat(format, target);
        }
    },
    MF_TIMESTAMP(MF_DATE.format + MF_TIME.format) {
        public String format(Object target) {
            return HBFormatter.dateFormat(format, target);
        }
    },

    ISO_DATE("yyyyMMdd'T''Z'") {
        public String format(Object target) {
            return HBFormatter.dateFormat(format, target);
        }
    },
    ISO_TIME("'T'HHmmssSSS'Z'") {
        public String format(Object target) {
            return HBFormatter.dateFormat(format, target);
        }
    },
    ISO_TIMESTAMP("yyyyMMdd'T'HHmmssSSS'Z'") {
        public String format(Object target) {
            return HBFormatter.dateFormat(format, target);
        }
    };

    public final String format;

    HBFormatter(String format) {
        this.format = format;
    }

    public abstract String format(Object target);

    private static String dateFormat(String pattern, Object date) {
        return nonNull(date) ? new SimpleDateFormat(pattern).format(date) : null;
    }

}
