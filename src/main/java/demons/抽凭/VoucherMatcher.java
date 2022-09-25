package demons.抽凭;

import org.jsoup.select.Elements;

@FunctionalInterface
public interface VoucherMatcher {
    String findVoucher(Elements pEls);
}
