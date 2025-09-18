
    // səhifə yüklənəndə URL-də ?error varsa, göstərir
    if (window.location.search.includes("error")) {
        // URL-dən parametri sil
        const newUrl = window.location.origin + window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    }
