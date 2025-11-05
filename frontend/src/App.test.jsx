import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import React from "react";
import App from "./App.jsx";

describe("App", () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ([
        {
          name: "next.js",
          htmlUrl: "https://github.com/vercel/next.js",
          stargazersCount: 123,
          forksCount: 10,
          language: "TypeScript",
          updatedAt: "2025-01-01T00:00:00Z",
          description: "The React Framework"
        }
      ])
    });
  });

  it("loads and shows cards", async () => {
    render(<App />);
    const btn = screen.getByRole("button", { name: /load/i });
    fireEvent.click(btn);

    await waitFor(() =>
      expect(screen.getByText(/next\.js/i)).toBeInTheDocument()
    );

    expect(fetch).toHaveBeenCalledTimes(1);
    expect(screen.getByText(/The React Framework/i)).toBeInTheDocument();
  });
});
