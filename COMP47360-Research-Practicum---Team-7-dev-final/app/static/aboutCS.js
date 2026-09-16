  // Get current file name and splits it by "/"
  const currentPage = window.location.pathname.split("/").pop();

  // Then loops through the button href's to find matching one
  document.querySelectorAll(".nav-button").forEach(link => {
    if (link.getAttribute("href") === currentPage) {
        // "active" applied if matching, which allows persistent css styling for active button
      link.classList.add("active");
    }
  });

  function mainPage() {
  window.location.href = "crowdScope.html";
}