document.addEventListener("DOMContentLoaded", function () {
    const pageLinks = document.querySelectorAll(".pagination .page-link");
    let size;

    // Ekran ölçüsünə görə size təyin edirik
    if (window.innerWidth <= 768) { // mobil üçün
        size = 2;
    } else { // desktop üçün
        size = 5;
    }

    // Hər linkdə size parametri əlavə edirik / dəyişdiririk
    pageLinks.forEach(link => {
        let url = new URL(link.href);
        url.searchParams.set("size", size);
        link.href = url.toString();
    });
});
